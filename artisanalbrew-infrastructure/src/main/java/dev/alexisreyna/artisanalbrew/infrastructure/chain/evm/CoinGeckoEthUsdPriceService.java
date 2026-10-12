package dev.alexisreyna.artisanalbrew.infrastructure.chain.evm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.EthUsdPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

/**
 * Port of {@code CoinGeckoEthUsdPriceService}: {@code simple/price} with a
 * 5-minute cache. Null on any failure — never a false zero.
 */
public class CoinGeckoEthUsdPriceService implements EthUsdPriceService {
    private static final Logger log = LoggerFactory.getLogger(CoinGeckoEthUsdPriceService.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Duration CACHE_DURATION = Duration.ofMinutes(5);

    private final HttpClient http;
    private final String baseUrl;
    private final ObjectMapper mapper;
    private volatile CachedPrice cached;

    public CoinGeckoEthUsdPriceService() {
        this(HttpClient.newBuilder().connectTimeout(TIMEOUT).build(), "https://api.coingecko.com/api/v3/", new ObjectMapper());
    }

    CoinGeckoEthUsdPriceService(HttpClient http, String baseUrl, ObjectMapper mapper) {
        this.http = http;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.mapper = mapper;
    }

    @Override
    public BigDecimal getEthUsdPrice() {
        CachedPrice hit = cached;
        if (hit != null && hit.computedAt().plus(CACHE_DURATION).isAfter(Instant.now())) {
            return hit.price();
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "simple/price?ids=ethereum&vs_currencies=usd"))
                    .timeout(TIMEOUT)
                    .header("User-Agent", "ArtisanalBrew/1.0")
                    .GET()
                    .build();
            HttpResponse<String> response =
                    http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("CoinGecko ETH/USD request failed with status {}", response.statusCode());
                return null;
            }
            JsonNode root = mapper.readTree(response.body());
            JsonNode usd = root.path("ethereum").path("usd");
            BigDecimal price = usd.isNumber() ? usd.decimalValue()
                    : usd.isTextual() ? new BigDecimal(usd.asText()) : null;
            if (price == null || price.signum() <= 0) {
                log.warn("CoinGecko ETH/USD response did not include a usable price.");
                return null;
            }
            cached = new CachedPrice(price, Instant.now());
            return price;
        } catch (Exception e) {
            log.warn("Could not fetch ETH/USD price from CoinGecko.", e);
            return null;
        }
    }

    private record CachedPrice(BigDecimal price, Instant computedAt) {
    }
}
