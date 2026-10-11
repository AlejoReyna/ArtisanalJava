package dev.alexisreyna.artisanalbrew.application.service.rewards;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;

/** Port of {@code ILoyaltyMintService} in {@code Rewards/ILoyaltyMintService.cs}. */
public interface LoyaltyMintService {
    LoyaltyMintResult mint(BlockchainNetworkOptions chain, LoyaltyMintCommand command);
}
