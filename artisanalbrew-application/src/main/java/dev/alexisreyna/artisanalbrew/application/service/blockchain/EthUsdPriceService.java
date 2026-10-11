package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.math.BigDecimal;

/** Port of {@code IEthUsdPriceService} in {@code Blockchain/IEthUsdPriceService.cs}. */
public interface EthUsdPriceService {
    BigDecimal getEthUsdPrice();
}
