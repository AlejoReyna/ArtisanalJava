package dev.alexisreyna.artisanalbrew.application.service.rewards;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.repository.RewardClaimRepository;
import dev.alexisreyna.artisanalbrew.application.repository.UnitOfWork;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeWeb3Service;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.TransactionVerificationStatus;
import dev.alexisreyna.artisanalbrew.domain.entities.RewardClaim;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Port of {@code LoyaltyMintService} in {@code Rewards/LoyaltyMintService.cs}.
 * Verify → record → mint as one atomic unit; a failed mint rolls the claim back.
 */
public final class DefaultLoyaltyMintService implements LoyaltyMintService {
    private static final int ALLOCATION_NAME_MAX_LENGTH = 120;

    private final CoffeeWeb3Service web3;
    private final RewardClaimRepository claims;
    private final UnitOfWork unitOfWork;

    public DefaultLoyaltyMintService(CoffeeWeb3Service web3, RewardClaimRepository claims, UnitOfWork unitOfWork) {
        this.web3 = web3;
        this.claims = claims;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public LoyaltyMintResult mint(BlockchainNetworkOptions chain, LoyaltyMintCommand command) {
        // Cheap pre-check; the authoritative one runs inside the transaction.
        if (claims.existsByPaymentHash(command.paymentTransactionHash())) {
            return LoyaltyMintResult.ALREADY_CLAIMED;
        }
        TransactionVerificationStatus status = web3.verifyPaymentTransaction(
                command.paymentTransactionHash(), command.walletAddress(), command.paymentAmount());
        if (status == TransactionVerificationStatus.PENDING_CONFIRMATIONS) {
            return LoyaltyMintResult.PENDING_CONFIRMATIONS;
        }
        if (status != TransactionVerificationStatus.VERIFIED) {
            return LoyaltyMintResult.PAYMENT_NOT_VERIFIED;
        }
        if (!web3.isMintingConfigured()) {
            return LoyaltyMintResult.MINTING_NOT_CONFIGURED;
        }
        BigDecimal mintAmount = RewardCalculator.loyaltyFromVerifiedPayment(command.paymentAmount());
        if (mintAmount.signum() <= 0) {
            return LoyaltyMintResult.PAYMENT_NOT_VERIFIED;
        }

        try (UnitOfWork.UnitOfWorkTransaction tx = unitOfWork.beginSerializableTransaction()) {
            // Re-checked under serializable isolation: two requests can clear the pre-check.
            if (claims.existsByPaymentHash(command.paymentTransactionHash())) {
                return LoyaltyMintResult.ALREADY_CLAIMED;
            }
            RewardClaim claim = buildClaim(chain, command, mintAmount);
            if (!claims.tryAdd(claim)) {
                return LoyaltyMintResult.ALREADY_CLAIMED;
            }
            String mintTransactionHash;
            try {
                mintTransactionHash = web3.mintCoffeeCoin(command.walletAddress(), mintAmount);
            } catch (RuntimeException e) {
                // No commit → the claim rolls back; the payment stays claimable.
                return LoyaltyMintResult.mintFailed(e.getMessage());
            }
            claim.setTransactionHash(mintTransactionHash);
            claim.setMintExplorerUrl(explorerTxUrl(chain, mintTransactionHash));
            claims.update(claim);
            tx.commit();
            return LoyaltyMintResult.minted(mintTransactionHash);
        }
    }

    private static RewardClaim buildClaim(BlockchainNetworkOptions chain, LoyaltyMintCommand command, BigDecimal mintAmount) {
        RewardClaim claim = new RewardClaim();
        claim.setWalletAddress(command.walletAddress());
        claim.setAmount(mintAmount);
        claim.setClaimType("allocation");
        claim.setPaymentTransactionHash(command.paymentTransactionHash());
        claim.setPaymentAmount(command.paymentAmount());
        claim.setPaymentChainId(chain.getChainId());
        claim.setPaymentNetworkName(chain.getNetworkName());
        claim.setPaymentTokenContract(chain.effectivePaymentTokenContract());
        claim.setMarketplaceWallet(chain.getMarketplaceWallet());
        claim.setAllocationName(normalizeAllocationName(command.allocationName()));
        claim.setPaymentExplorerUrl(explorerTxUrl(chain, command.paymentTransactionHash()));
        claim.setClaimedAtUtc(Instant.now());
        return claim;
    }

    private static String normalizeAllocationName(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.substring(0, Math.min(normalized.length(), ALLOCATION_NAME_MAX_LENGTH));
    }

    private static String explorerTxUrl(BlockchainNetworkOptions chain, String transactionHash) {
        String explorer = chain.getExplorerUrl() == null ? "" : chain.getExplorerUrl().trim();
        if (explorer.isEmpty()) {
            return "";
        }
        return explorer.replaceAll("/+$", "") + "/tx/" + transactionHash;
    }
}
