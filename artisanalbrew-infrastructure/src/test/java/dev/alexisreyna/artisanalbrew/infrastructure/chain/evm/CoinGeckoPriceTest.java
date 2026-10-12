package dev.alexisreyna.artisanalbrew.infrastructure.chain.evm;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CoinGeckoPriceTest {

    private MockWebServer server;

    @AfterEach
    void stop() throws Exception {
        if (server != null) {
            server.shutdown();
        }
    }

    @Test
    void parsesPriceAndCaches() throws Exception {
        server = new MockWebServer();
        server.enqueue(new MockResponse().setBody("{\"ethereum\":{\"usd\":3750.5}}"));
        server.start();
        var service = new CoinGeckoEthUsdPriceService(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
                server.url("/").toString(), new ObjectMapper());

        assertThat(service.getEthUsdPrice()).isEqualByComparingTo("3750.5");
        // Second call is cached: no further HTTP interaction needed.
        assertThat(service.getEthUsdPrice()).isEqualByComparingTo("3750.5");
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void nullOnUnusableResponses() throws Exception {
        server = new MockWebServer();
        server.enqueue(new MockResponse().setBody("{\"ethereum\":{}}"));
        server.enqueue(new MockResponse().setResponseCode(500));
        server.start();
        var service = new CoinGeckoEthUsdPriceService(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
                server.url("/").toString(), new ObjectMapper());

        assertThat(service.getEthUsdPrice()).isNull();
        assertThat(service.getEthUsdPrice()).isNull();
    }
}
