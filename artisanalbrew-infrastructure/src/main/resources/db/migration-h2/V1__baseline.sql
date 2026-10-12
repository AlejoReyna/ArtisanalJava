-- Flyway baseline V1: full schema squashed from 33 EF Core migrations.
-- Source of truth: ThisCafeteria.Infrastructure persistence model snapshot.
-- Naming (tables, columns, indexes) mirrors EF Core conventions exactly so
-- existing PostgreSQL data keeps working. Two deliberate divergences:
--   1. jsonb/text columns are VARCHAR here (Avatar, PayloadJson).
--      PostgreSQL treats VARCHAR and TEXT identically; this keeps Hibernate validate green on H2.
-- H2 twin of V1: identical except filtered-index WHERE clauses (see above).
--   2. No EF __EFMigrationsHistory table: Flyway tracks history itself.

CREATE TABLE "AgentDirectoryEntries" (
    "Id" UUID NOT NULL,
    "AgentId" BIGINT NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "CreatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "IsActive" BOOLEAN NOT NULL,
    "MetadataUri" VARCHAR(2048) NOT NULL,
    "OwnerAddress" VARCHAR(128) NOT NULL,
    "RegistryAddress" VARCHAR(128) NOT NULL,
    "UpdatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT "PK_AgentDirectoryEntries" PRIMARY KEY ("Id")
);

CREATE TABLE "AgentFeedback" (
    "Id" UUID NOT NULL,
    "AgentId" BIGINT NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "CommentUri" VARCHAR(2048) NOT NULL,
    "CreatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "JobId" BIGINT NOT NULL,
    "RegistryAddress" VARCHAR(128) NOT NULL,
    "ReviewerAddress" VARCHAR(128) NOT NULL,
    "Score" BIGINT NOT NULL,
    CONSTRAINT "PK_AgentFeedback" PRIMARY KEY ("Id")
);

CREATE TABLE "AgentPermissionEpochs" (
    "Id" UUID NOT NULL,
    "AgentAddress" VARCHAR(128) NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "ConcurrencyToken" INTEGER NOT NULL,
    "CreatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "DelegatorAddress" VARCHAR(128) NOT NULL,
    "Epoch" VARCHAR(80) NOT NULL,
    "InstalledAtUtc" TIMESTAMP WITH TIME ZONE,
    "InstalledTxHash" VARCHAR(128),
    "OwnerAddress" VARCHAR(128) NOT NULL,
    "RevokedAtUtc" TIMESTAMP WITH TIME ZONE,
    "RevokedTxHash" VARCHAR(128),
    "SmartAccountRecordId" UUID NOT NULL,
    "Status" INTEGER NOT NULL,
    "UpdatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "ValidAfterUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "ValidBeforeUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT "PK_AgentPermissionEpochs" PRIMARY KEY ("Id")
);

CREATE TABLE "AgentPermissionGrants" (
    "Id" UUID NOT NULL,
    "AmountWei" VARCHAR(80) NOT NULL,
    "CreatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "DelegationHash" VARCHAR(66) NOT NULL,
    "Description" VARCHAR(64) NOT NULL,
    "EpochId" UUID NOT NULL,
    "Selector" VARCHAR(10) NOT NULL,
    "TargetAddress" VARCHAR(128) NOT NULL,
    "TokenAddress" VARCHAR(128),
    CONSTRAINT "PK_AgentPermissionGrants" PRIMARY KEY ("Id")
);

CREATE TABLE "AgenticCommerceCheckpoints" (
    "Id" UUID NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "EscrowAddress" VARCHAR(128) NOT NULL,
    "LastScannedBlock" BIGINT NOT NULL,
    "UpdatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT "PK_AgenticCommerceCheckpoints" PRIMARY KEY ("Id")
);

CREATE TABLE "AgenticJobAppliedEvents" (
    "Id" UUID NOT NULL,
    "AppliedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "ContractAddress" VARCHAR(128) NOT NULL,
    "LogIndex" INTEGER NOT NULL,
    "TransactionHash" VARCHAR(128) NOT NULL,
    CONSTRAINT "PK_AgenticJobAppliedEvents" PRIMARY KEY ("Id")
);

CREATE TABLE "AgenticJobDeferredEvents" (
    "Id" UUID NOT NULL,
    "BlockNumber" BIGINT NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "ContractAddress" VARCHAR(128) NOT NULL,
    "DeferralReason" VARCHAR(512) NOT NULL,
    "DeferredAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "EventType" VARCHAR(64) NOT NULL,
    "LogIndex" INTEGER NOT NULL,
    "OnChainJobId" BIGINT NOT NULL,
    "TransactionHash" VARCHAR(128) NOT NULL,
    CONSTRAINT "PK_AgenticJobDeferredEvents" PRIMARY KEY ("Id")
);

CREATE TABLE "AgenticJobs" (
    "Id" UUID NOT NULL,
    "Budget" NUMERIC NOT NULL,
    "ChainId" BIGINT NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "ClientAddress" VARCHAR(128) NOT NULL,
    "CompletionTransactionHash" VARCHAR NOT NULL,
    "ConcurrencyToken" BIGINT NOT NULL,
    "ContractAddress" VARCHAR NOT NULL,
    "CreatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "CreationTransactionHash" VARCHAR NOT NULL,
    "DecisionReason" VARCHAR(1024),
    "DeliverableCommitment" VARCHAR(1024),
    "DescriptionCommitment" VARCHAR(1024) NOT NULL,
    "EscrowAddress" VARCHAR(128) NOT NULL,
    "EvaluatorAddress" VARCHAR(128) NOT NULL,
    "ExpiredAt" BIGINT NOT NULL,
    "FundedTransactionHash" VARCHAR NOT NULL,
    "JobId" BIGINT NOT NULL,
    "LastReconciledBlock" BIGINT NOT NULL,
    "OnChainJobId" BIGINT NOT NULL,
    "ProviderAddress" VARCHAR(128) NOT NULL,
    "Status" VARCHAR(32) NOT NULL,
    "UpdatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT "PK_AgenticJobs" PRIMARY KEY ("Id")
);

CREATE TABLE "AspNetUsers" (
    "Id" VARCHAR NOT NULL,
    "AccessFailedCount" INTEGER NOT NULL,
    "ConcurrencyStamp" VARCHAR,
    "Email" VARCHAR(256),
    "EmailConfirmed" BOOLEAN NOT NULL,
    "LockoutEnabled" BOOLEAN NOT NULL,
    "LockoutEnd" TIMESTAMP WITH TIME ZONE,
    "NormalizedEmail" VARCHAR(256),
    "NormalizedUserName" VARCHAR(256),
    "PasswordHash" VARCHAR,
    "PhoneNumber" VARCHAR,
    "PhoneNumberConfirmed" BOOLEAN NOT NULL,
    "SecurityStamp" VARCHAR,
    "TwoFactorEnabled" BOOLEAN NOT NULL,
    "UserName" VARCHAR(256),
    "UserProfileId" UUID,
    "WalletAddress" VARCHAR(128),
    "WalletChainId" INTEGER,
    "WalletVerifiedAt" TIMESTAMP WITH TIME ZONE,
    CONSTRAINT "PK_AspNetUsers" PRIMARY KEY ("Id")
);

CREATE TABLE "CartItems" (
    "Id" UUID NOT NULL,
    "CartId" UUID NOT NULL,
    "ProductId" UUID NOT NULL,
    "ProductName" VARCHAR(160) NOT NULL,
    "Quantity" INTEGER NOT NULL,
    "UnitPrice" NUMERIC(18,2) NOT NULL,
    CONSTRAINT "PK_CartItems" PRIMARY KEY ("Id")
);

CREATE TABLE "Carts" (
    "Id" UUID NOT NULL,
    "CreatedAt" TIMESTAMP WITH TIME ZONE NOT NULL,
    "UpdatedAt" TIMESTAMP WITH TIME ZONE,
    "UserProfileId" UUID NOT NULL,
    CONSTRAINT "PK_Carts" PRIMARY KEY ("Id")
);

CREATE TABLE "CouponRedemptions" (
    "Id" UUID NOT NULL,
    "CouponId" UUID NOT NULL,
    "OrderId" UUID NOT NULL,
    "RedeemedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "UserProfileId" UUID NOT NULL,
    CONSTRAINT "PK_CouponRedemptions" PRIMARY KEY ("Id")
);

CREATE TABLE "Coupons" (
    "Id" UUID NOT NULL,
    "Code" VARCHAR(64) NOT NULL,
    "CreatedAt" TIMESTAMP WITH TIME ZONE NOT NULL,
    "DiscountPercent" NUMERIC(5,2) NOT NULL,
    "IsActive" BOOLEAN NOT NULL,
    "MinimumOrderTotal" NUMERIC(18,2) NOT NULL,
    "NormalizedCode" VARCHAR(64) NOT NULL,
    "UpdatedAt" TIMESTAMP WITH TIME ZONE,
    CONSTRAINT "PK_Coupons" PRIMARY KEY ("Id")
);

CREATE TABLE "CrossChainSolverCheckpoints" (
    "Id" UUID NOT NULL,
    "LastScannedBlock" BIGINT NOT NULL,
    "SourceChainKey" VARCHAR(64) NOT NULL,
    "SourceResolverAddress" VARCHAR(128) NOT NULL,
    "UpdatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT "PK_CrossChainSolverCheckpoints" PRIMARY KEY ("Id")
);

CREATE TABLE "CrossChainSolverFills" (
    "Id" UUID NOT NULL,
    "DenialReason" VARCHAR(256),
    "EvaluatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "FillTransactionHash" VARCHAR(128),
    "Filled" BOOLEAN NOT NULL,
    "OrderId" VARCHAR(66) NOT NULL,
    "SourceChainKey" VARCHAR(64) NOT NULL,
    "SourceResolverAddress" VARCHAR(128) NOT NULL,
    "SubmitTransactionHash" VARCHAR(128) NOT NULL,
    CONSTRAINT "PK_CrossChainSolverFills" PRIMARY KEY ("Id")
);

CREATE TABLE "OrderItems" (
    "Id" UUID NOT NULL,
    "OrderId" UUID NOT NULL,
    "ProductId" UUID NOT NULL,
    "ProductName" VARCHAR(160) NOT NULL,
    "Quantity" INTEGER NOT NULL,
    "Total" NUMERIC(18,2) NOT NULL,
    "UnitPrice" NUMERIC(18,2) NOT NULL,
    CONSTRAINT "PK_OrderItems" PRIMARY KEY ("Id")
);

CREATE TABLE "Orders" (
    "Id" UUID NOT NULL,
    "CouponCode" VARCHAR(64),
    "CouponDiscountPercent" NUMERIC(5,2),
    "CouponId" UUID,
    "CreatedAt" TIMESTAMP WITH TIME ZONE NOT NULL,
    "DiscountAmount" NUMERIC(18,2) NOT NULL,
    "OrderNumber" VARCHAR(64) NOT NULL,
    "PaidAtUtc" TIMESTAMP WITH TIME ZONE,
    "PaymentChainId" INTEGER,
    "PaymentEthAmount" NUMERIC(28,18),
    "PaymentExplorerUrl" VARCHAR(2048),
    "PaymentNetworkName" VARCHAR(80),
    "PaymentTransactionHash" VARCHAR(66),
    "Shipping" NUMERIC(18,2) NOT NULL,
    "Status" VARCHAR(80) NOT NULL,
    "Subtotal" NUMERIC(18,2) NOT NULL,
    "Tax" NUMERIC(18,2) NOT NULL,
    "Total" NUMERIC(18,2) NOT NULL,
    "UpdatedAt" TIMESTAMP WITH TIME ZONE,
    "UserProfileId" UUID NOT NULL,
    "WalletAddress" VARCHAR(42) NOT NULL,
    CONSTRAINT "PK_Orders" PRIMARY KEY ("Id")
);

CREATE TABLE "Products" (
    "Id" UUID NOT NULL,
    "Category" VARCHAR(80) NOT NULL,
    "CreatedAt" TIMESTAMP WITH TIME ZONE NOT NULL,
    "Description" VARCHAR(1000) NOT NULL,
    "ImageUrl" VARCHAR(2048),
    "IsActive" BOOLEAN NOT NULL,
    "Name" VARCHAR(160) NOT NULL,
    "Origin" VARCHAR(120),
    "Price" NUMERIC(18,2) NOT NULL,
    "Process" VARCHAR(40),
    "RoastLevel" VARCHAR(40),
    "Slug" VARCHAR(180) NOT NULL,
    "StockQuantity" INTEGER NOT NULL,
    "UpdatedAt" TIMESTAMP WITH TIME ZONE,
    CONSTRAINT "PK_Products" PRIMARY KEY ("Id")
);

CREATE TABLE "Receipts" (
    "Id" UUID NOT NULL,
    "CreatedAt" TIMESTAMP WITH TIME ZONE NOT NULL,
    "EmailSentAt" TIMESTAMP WITH TIME ZONE,
    "FileUrl" VARCHAR(2048) NOT NULL,
    "OrderId" UUID NOT NULL,
    CONSTRAINT "PK_Receipts" PRIMARY KEY ("Id")
);

CREATE TABLE "RewardClaims" (
    "Id" UUID NOT NULL,
    "AllocationName" VARCHAR(120),
    "Amount" NUMERIC(36,18) NOT NULL,
    "ClaimType" VARCHAR(32) NOT NULL,
    "ClaimedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "MarketplaceWallet" VARCHAR(42),
    "MintExplorerUrl" VARCHAR(2048),
    "PaymentAmount" NUMERIC(36,18),
    "PaymentChainId" INTEGER,
    "PaymentExplorerUrl" VARCHAR(2048),
    "PaymentNetworkName" VARCHAR(80),
    "PaymentTokenContract" VARCHAR(42),
    "PaymentTransactionHash" VARCHAR(66),
    "TransactionHash" VARCHAR(66),
    "WalletAddress" VARCHAR(42) NOT NULL,
    CONSTRAINT "PK_RewardClaims" PRIMARY KEY ("Id")
);

CREATE TABLE "SmartAccountRecords" (
    "Id" UUID NOT NULL,
    "AccountAddress" VARCHAR(128) NOT NULL,
    "AccountType" INTEGER NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "DeployedAtUtc" TIMESTAMP WITH TIME ZONE,
    "DiscoveredAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "FactoryAddress" VARCHAR(128) NOT NULL,
    "ImplementationVerified" BOOLEAN NOT NULL,
    "IsDeployed" BOOLEAN NOT NULL,
    "OwnerAddress" VARCHAR(128) NOT NULL,
    "Salt" VARCHAR(80) NOT NULL,
    "UpdatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT "PK_SmartAccountRecords" PRIMARY KEY ("Id")
);

CREATE TABLE "SolanaFaucetClaims" (
    "Id" UUID NOT NULL,
    "Amount" NUMERIC NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "ClaimedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "RawAmount" BIGINT NOT NULL,
    "Signature" VARCHAR(128) NOT NULL,
    "WalletAddress" VARCHAR(128) NOT NULL,
    CONSTRAINT "PK_SolanaFaucetClaims" PRIMARY KEY ("Id")
);

CREATE TABLE "SponsorshipGrants" (
    "Id" UUID NOT NULL,
    "BudgetUsd" NUMERIC(18,6) NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "ConcurrencyToken" INTEGER NOT NULL,
    "CreatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "MaxOperationCostUsd" NUMERIC(18,6) NOT NULL,
    "OwnerAddress" VARCHAR(128) NOT NULL,
    "RevertedOperationCount" INTEGER NOT NULL,
    "RevokedAtUtc" TIMESTAMP WITH TIME ZONE,
    "SpentUsd" NUMERIC(18,6) NOT NULL,
    "UpdatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "ValidFromUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "ValidUntilUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT "PK_SponsorshipGrants" PRIMARY KEY ("Id")
);

CREATE TABLE "SponsorshipUsages" (
    "Id" UUID NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "CostUsd" NUMERIC(18,6) NOT NULL,
    "GrantId" UUID NOT NULL,
    "OwnerAddress" VARCHAR(128) NOT NULL,
    "RecordedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "Selector" VARCHAR(10) NOT NULL,
    "TargetAddress" VARCHAR(128) NOT NULL,
    CONSTRAINT "PK_SponsorshipUsages" PRIMARY KEY ("Id")
);

CREATE TABLE "StakingLedgerEntries" (
    "Id" UUID NOT NULL,
    "ActionType" VARCHAR(32) NOT NULL,
    "Amount" NUMERIC(36,18) NOT NULL,
    "AssetAmount" NUMERIC(36,18) NOT NULL,
    "AssetIdentifier" VARCHAR(128) NOT NULL,
    "BlockOrSlot" BIGINT NOT NULL,
    "ChainId" INTEGER NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "ExplorerUrl" VARCHAR(2048) NOT NULL,
    "Family" VARCHAR(16) NOT NULL,
    "NetworkName" VARCHAR(80) NOT NULL,
    "OccurredAtUtc" TIMESTAMP WITH TIME ZONE,
    "OperationIndex" INTEGER NOT NULL,
    "PaymentTokenContract" VARCHAR(128) NOT NULL,
    "RawAssetAmount" VARCHAR(80) NOT NULL,
    "RawRewardAmount" VARCHAR(80) NOT NULL,
    "RawShareAmount" VARCHAR(80) NOT NULL,
    "ReceiptIdentifier" VARCHAR(128) NOT NULL,
    "RecordedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    "RewardAmount" NUMERIC(36,18) NOT NULL,
    "RewardIdentifier" VARCHAR(128) NOT NULL,
    "ShareAmount" NUMERIC(36,18) NOT NULL,
    "StakingPoolContract" VARCHAR(128) NOT NULL,
    "TransactionHash" VARCHAR(256) NOT NULL,
    "VaultOrProgramIdentifier" VARCHAR(128) NOT NULL,
    "VerificationState" VARCHAR(32) NOT NULL,
    "Verified" BOOLEAN NOT NULL,
    "WalletAddress" VARCHAR(128) NOT NULL,
    CONSTRAINT "PK_StakingLedgerEntries" PRIMARY KEY ("Id")
);

CREATE TABLE "StakingReconciliationCheckpoints" (
    "Id" UUID NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "CursorType" VARCHAR(16) NOT NULL,
    "Family" VARCHAR(16) NOT NULL,
    "LastScannedBlock" BIGINT NOT NULL,
    "LastScannedSignature" VARCHAR(256) NOT NULL,
    "LastScannedSlot" BIGINT NOT NULL,
    "SourceIdentifier" VARCHAR(128) NOT NULL,
    "StakingPoolContract" VARCHAR(128) NOT NULL,
    "UpdatedAtUtc" TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT "PK_StakingReconciliationCheckpoints" PRIMARY KEY ("Id")
);

CREATE TABLE "TransparencyRecords" (
    "Id" UUID NOT NULL,
    "ChainId" INTEGER NOT NULL,
    "ContractAddress" VARCHAR(42) NOT NULL,
    "CreatedAt" TIMESTAMP WITH TIME ZONE NOT NULL,
    "ExplorerUrl" VARCHAR(2048) NOT NULL,
    "NetworkName" VARCHAR(80) NOT NULL,
    "OrderHash" VARCHAR(66) NOT NULL,
    "OrderId" UUID NOT NULL,
    "OrderNumber" VARCHAR(64) NOT NULL,
    "ProductName" VARCHAR(200) NOT NULL,
    "Quantity" INTEGER NOT NULL,
    "RecordedOnChainAt" TIMESTAMP WITH TIME ZONE,
    "Status" VARCHAR(40) NOT NULL,
    "Total" NUMERIC(18,2) NOT NULL,
    "TransactionHash" VARCHAR(66) NOT NULL,
    CONSTRAINT "PK_TransparencyRecords" PRIMARY KEY ("Id")
);

CREATE TABLE "UserProfiles" (
    "Id" UUID NOT NULL,
    "CreatedAt" TIMESTAMP WITH TIME ZONE NOT NULL,
    "DisplayName" VARCHAR(160) NOT NULL,
    "Email" VARCHAR(320) NOT NULL,
    "Role" VARCHAR(80) NOT NULL,
    "Avatar" VARCHAR,
    CONSTRAINT "PK_UserProfiles" PRIMARY KEY ("Id")
);

CREATE TABLE "WalletAuthChallenges" (
    "Id" UUID NOT NULL,
    "ChainKey" VARCHAR(64) NOT NULL,
    "ConsumedAtUtc" TIMESTAMP WITH TIME ZONE,
    "ExpiresAtUtc" TIMESTAMP WITH TIME ZONE,
    "IssuedAtUtc" TIMESTAMP WITH TIME ZONE,
    "MessageHash" VARCHAR(64) NOT NULL,
    "NonceHash" VARCHAR(64) NOT NULL,
    "Origin" VARCHAR(512) NOT NULL,
    "PublicKey" VARCHAR(128) NOT NULL,
    "VerificationAttempts" INTEGER NOT NULL,
    CONSTRAINT "PK_WalletAuthChallenges" PRIMARY KEY ("Id")
);

CREATE TABLE "WalletIdentities" (
    "Id" UUID NOT NULL,
    "DisplayAddress" VARCHAR(128) NOT NULL,
    "Family" VARCHAR(16) NOT NULL,
    "NormalizedAddress" VARCHAR(128) NOT NULL,
    "UserId" VARCHAR(450) NOT NULL,
    "VerifiedAtUtc" TIMESTAMP WITH TIME ZONE,
    "WalletProvider" VARCHAR(64) NOT NULL,
    CONSTRAINT "PK_WalletIdentities" PRIMARY KEY ("Id")
);

CREATE TABLE "wallet_status_events" (
    "Id" UUID NOT NULL,
    "AwsMessageId" VARCHAR,
    "CreatedAt" TIMESTAMP WITH TIME ZONE,
    "EventType" VARCHAR,
    "PayloadJson" VARCHAR,
    "PublishedToAwsAtUtc" TIMESTAMP WITH TIME ZONE,
    "Status" VARCHAR NOT NULL,
    "WalletAddress" VARCHAR NOT NULL,
    CONSTRAINT "PK_wallet_status_events" PRIMARY KEY ("Id")
);

CREATE TABLE "AspNetRoleClaims" (
    "Id" INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    "ClaimType" VARCHAR,
    "ClaimValue" VARCHAR,
    "RoleId" VARCHAR NOT NULL,
    CONSTRAINT "PK_AspNetRoleClaims" PRIMARY KEY ("Id")
);

CREATE TABLE "AspNetRoles" (
    "Id" VARCHAR NOT NULL,
    "ConcurrencyStamp" VARCHAR,
    "Name" VARCHAR(256),
    "NormalizedName" VARCHAR(256),
    CONSTRAINT "PK_AspNetRoles" PRIMARY KEY ("Id")
);

CREATE TABLE "AspNetUserClaims" (
    "Id" INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    "ClaimType" VARCHAR,
    "ClaimValue" VARCHAR,
    "UserId" VARCHAR NOT NULL,
    CONSTRAINT "PK_AspNetUserClaims" PRIMARY KEY ("Id")
);

CREATE TABLE "AspNetUserLogins" (
    "LoginProvider" VARCHAR NOT NULL,
    "ProviderKey" VARCHAR NOT NULL,
    "ProviderDisplayName" VARCHAR,
    "UserId" VARCHAR NOT NULL,
    CONSTRAINT "PK_AspNetUserLogins" PRIMARY KEY ("LoginProvider", "ProviderKey")
);

CREATE TABLE "AspNetUserRoles" (
    "UserId" VARCHAR NOT NULL,
    "RoleId" VARCHAR NOT NULL,
    CONSTRAINT "PK_AspNetUserRoles" PRIMARY KEY ("UserId", "RoleId")
);

CREATE TABLE "AspNetUserTokens" (
    "UserId" VARCHAR NOT NULL,
    "LoginProvider" VARCHAR NOT NULL,
    "Name" VARCHAR NOT NULL,
    "Value" VARCHAR,
    CONSTRAINT "PK_AspNetUserTokens" PRIMARY KEY ("UserId", "LoginProvider", "Name")
);

CREATE INDEX "IX_AgentDirectoryEntries_OwnerAddress" ON "AgentDirectoryEntries" ("OwnerAddress");
CREATE UNIQUE INDEX "IX_AgentDirectoryEntries_ChainKey_RegistryAddress_AgentId" ON "AgentDirectoryEntries" ("ChainKey", "RegistryAddress", "AgentId");

CREATE INDEX "IX_AgentFeedback_ReviewerAddress" ON "AgentFeedback" ("ReviewerAddress");
CREATE UNIQUE INDEX "IX_AgentFeedback_ChainKey_RegistryAddress_AgentId_JobId" ON "AgentFeedback" ("ChainKey", "RegistryAddress", "AgentId", "JobId");

CREATE INDEX "IX_AgentPermissionEpochs_ChainDelegator" ON "AgentPermissionEpochs" ("ChainKey", "DelegatorAddress");
CREATE UNIQUE INDEX "IX_AgentPermissionEpochs_AccountEpoch" ON "AgentPermissionEpochs" ("ChainKey", "SmartAccountRecordId", "Epoch");

CREATE INDEX "IX_AgentPermissionGrants_DelegationHash" ON "AgentPermissionGrants" ("DelegationHash");
CREATE INDEX "IX_AgentPermissionGrants_Epoch" ON "AgentPermissionGrants" ("EpochId");

CREATE UNIQUE INDEX "IX_AgenticCommerceCheckpoints_ChainKey_EscrowAddress" ON "AgenticCommerceCheckpoints" ("ChainKey", "EscrowAddress");

CREATE INDEX "IX_AgenticJobDeferredEvents_Job" ON "AgenticJobDeferredEvents" ("ChainKey", "ContractAddress", "OnChainJobId");
CREATE UNIQUE INDEX "IX_AgenticJobDeferredEvents_LogIdentity" ON "AgenticJobDeferredEvents" ("ChainKey", "ContractAddress", "TransactionHash", "LogIndex");

CREATE INDEX "IX_AgenticJobs_ClientAddress" ON "AgenticJobs" ("ClientAddress");
CREATE INDEX "IX_AgenticJobs_EvaluatorAddress" ON "AgenticJobs" ("EvaluatorAddress");
CREATE INDEX "IX_AgenticJobs_ProviderAddress" ON "AgenticJobs" ("ProviderAddress");
CREATE UNIQUE INDEX "IX_AgenticJobs_ChainKey_ContractAddress_OnChainJobId" ON "AgenticJobs" ("ChainKey", "ContractAddress", "OnChainJobId");

CREATE INDEX "EmailIndex" ON "AspNetUsers" ("NormalizedEmail");
CREATE UNIQUE INDEX "UserNameIndex" ON "AspNetUsers" ("NormalizedUserName");
CREATE UNIQUE INDEX "IX_AspNetUsers_WalletAddress" ON "AspNetUsers" ("WalletAddress");  -- WHERE dropped: H2 has no partial indexes; NULLs stay distinct in UNIQUE indexes on both engines.

CREATE INDEX "IX_CartItems_CartId" ON "CartItems" ("CartId");
CREATE INDEX "IX_CartItems_ProductId" ON "CartItems" ("ProductId");

CREATE UNIQUE INDEX "IX_Carts_UserProfileId" ON "Carts" ("UserProfileId");

CREATE UNIQUE INDEX "IX_CouponRedemptions_OrderId" ON "CouponRedemptions" ("OrderId");
CREATE INDEX "IX_CouponRedemptions_UserProfileId" ON "CouponRedemptions" ("UserProfileId");
CREATE UNIQUE INDEX "IX_CouponRedemptions_CouponId_UserProfileId" ON "CouponRedemptions" ("CouponId", "UserProfileId");

CREATE UNIQUE INDEX "IX_Coupons_NormalizedCode" ON "Coupons" ("NormalizedCode");

CREATE UNIQUE INDEX "IX_CrossChainSolverCheckpoints_SourceChainResolver" ON "CrossChainSolverCheckpoints" ("SourceChainKey", "SourceResolverAddress");

CREATE UNIQUE INDEX "IX_CrossChainSolverFills_Identity" ON "CrossChainSolverFills" ("SourceChainKey", "SourceResolverAddress", "OrderId");

CREATE INDEX "IX_OrderItems_OrderId" ON "OrderItems" ("OrderId");

CREATE INDEX "IX_Orders_CouponId" ON "Orders" ("CouponId");
CREATE UNIQUE INDEX "IX_Orders_OrderNumber" ON "Orders" ("OrderNumber");
CREATE UNIQUE INDEX "IX_Orders_PaymentTransactionHash" ON "Orders" ("PaymentTransactionHash");  -- WHERE dropped: H2 has no partial indexes; NULLs stay distinct in UNIQUE indexes on both engines.
CREATE INDEX "IX_Orders_UserProfileId" ON "Orders" ("UserProfileId");
CREATE INDEX "IX_Orders_WalletAddress" ON "Orders" ("WalletAddress");

CREATE UNIQUE INDEX "IX_Products_Slug" ON "Products" ("Slug");

CREATE UNIQUE INDEX "IX_Receipts_OrderId" ON "Receipts" ("OrderId");

CREATE UNIQUE INDEX "IX_RewardClaims_PaymentTransactionHash" ON "RewardClaims" ("PaymentTransactionHash");  -- WHERE dropped: H2 has no partial indexes; NULLs stay distinct in UNIQUE indexes on both engines.
CREATE INDEX "IX_RewardClaims_TransactionHash" ON "RewardClaims" ("TransactionHash");
CREATE INDEX "IX_RewardClaims_WalletAddress_ClaimedAtUtc" ON "RewardClaims" ("WalletAddress", "ClaimedAtUtc");

CREATE INDEX "IX_SmartAccountRecords_ChainAddress" ON "SmartAccountRecords" ("ChainKey", "AccountAddress");
CREATE UNIQUE INDEX "IX_SmartAccountRecords_ChainOwnerType" ON "SmartAccountRecords" ("ChainKey", "OwnerAddress", "AccountType");

CREATE INDEX "IX_SolanaFaucetClaims_ChainKey_WalletAddress_ClaimedAtUtc" ON "SolanaFaucetClaims" ("ChainKey", "WalletAddress", "ClaimedAtUtc");

CREATE UNIQUE INDEX "IX_SponsorshipGrants_ChainOwner" ON "SponsorshipGrants" ("ChainKey", "OwnerAddress");

CREATE INDEX "IX_SponsorshipUsages_Grant" ON "SponsorshipUsages" ("GrantId");
CREATE INDEX "IX_SponsorshipUsages_ChainOwner" ON "SponsorshipUsages" ("ChainKey", "OwnerAddress");

CREATE INDEX "IX_StakingLedgerEntries_WalletAddress_RecordedAtUtc" ON "StakingLedgerEntries" ("WalletAddress", "RecordedAtUtc");
CREATE UNIQUE INDEX "IX_StakingLedgerEntries_ChainKey_TransactionHash_OperationIndex" ON "StakingLedgerEntries" ("ChainKey", "TransactionHash", "OperationIndex");

CREATE UNIQUE INDEX "IX_StakingReconciliationCheckpoints_ChainKey_SourceIdentifier" ON "StakingReconciliationCheckpoints" ("ChainKey", "SourceIdentifier");

CREATE INDEX "IX_TransparencyRecords_OrderHash" ON "TransparencyRecords" ("OrderHash");
CREATE INDEX "IX_TransparencyRecords_OrderId" ON "TransparencyRecords" ("OrderId");
CREATE INDEX "IX_TransparencyRecords_TransactionHash" ON "TransparencyRecords" ("TransactionHash");

CREATE UNIQUE INDEX "IX_UserProfiles_Email" ON "UserProfiles" ("Email");

CREATE UNIQUE INDEX "IX_WalletAuthChallenges_NonceHash" ON "WalletAuthChallenges" ("NonceHash");
CREATE INDEX "IX_WalletAuthChallenges_ExpiresAtUtc_ConsumedAtUtc" ON "WalletAuthChallenges" ("ExpiresAtUtc", "ConsumedAtUtc");
CREATE INDEX "IX_WalletAuthChallenges_PublicKey_ChainKey" ON "WalletAuthChallenges" ("PublicKey", "ChainKey");

CREATE INDEX "IX_WalletIdentities_UserId" ON "WalletIdentities" ("UserId");
CREATE UNIQUE INDEX "IX_WalletIdentities_Family_NormalizedAddress" ON "WalletIdentities" ("Family", "NormalizedAddress");

CREATE INDEX "IX_wallet_status_events_AwsMessageId" ON "wallet_status_events" ("AwsMessageId");
CREATE INDEX "IX_wallet_status_events_Status" ON "wallet_status_events" ("Status");
CREATE INDEX "ix_wallet_status_events_wallet_created_at" ON "wallet_status_events" ("WalletAddress", "CreatedAt");

CREATE INDEX "IX_AspNetRoleClaims_RoleId" ON "AspNetRoleClaims" ("RoleId");

CREATE UNIQUE INDEX "RoleNameIndex" ON "AspNetRoles" ("NormalizedName");

CREATE INDEX "IX_AspNetUserClaims_UserId" ON "AspNetUserClaims" ("UserId");

CREATE INDEX "IX_AspNetUserLogins_UserId" ON "AspNetUserLogins" ("UserId");

CREATE INDEX "IX_AspNetUserRoles_RoleId" ON "AspNetUserRoles" ("RoleId");

ALTER TABLE "AspNetRoleClaims" ADD CONSTRAINT "FK_AspNetRoleClaims_RoleId" FOREIGN KEY ("RoleId") REFERENCES "AspNetRoles" ("Id") ON DELETE CASCADE;
ALTER TABLE "AspNetUserClaims" ADD CONSTRAINT "FK_AspNetUserClaims_UserId" FOREIGN KEY ("UserId") REFERENCES "AspNetUsers" ("Id") ON DELETE CASCADE;
ALTER TABLE "AspNetUserLogins" ADD CONSTRAINT "FK_AspNetUserLogins_UserId" FOREIGN KEY ("UserId") REFERENCES "AspNetUsers" ("Id") ON DELETE CASCADE;
ALTER TABLE "AspNetUserRoles" ADD CONSTRAINT "FK_AspNetUserRoles_RoleId" FOREIGN KEY ("RoleId") REFERENCES "AspNetRoles" ("Id") ON DELETE CASCADE;
ALTER TABLE "AspNetUserRoles" ADD CONSTRAINT "FK_AspNetUserRoles_UserId" FOREIGN KEY ("UserId") REFERENCES "AspNetUsers" ("Id") ON DELETE CASCADE;
ALTER TABLE "AspNetUserTokens" ADD CONSTRAINT "FK_AspNetUserTokens_UserId" FOREIGN KEY ("UserId") REFERENCES "AspNetUsers" ("Id") ON DELETE CASCADE;
ALTER TABLE "AgentPermissionGrants" ADD CONSTRAINT "FK_AgentPermissionGrants_EpochId" FOREIGN KEY ("EpochId") REFERENCES "AgentPermissionEpochs" ("Id") ON DELETE CASCADE;
ALTER TABLE "Carts" ADD CONSTRAINT "FK_Carts_UserProfileId" FOREIGN KEY ("UserProfileId") REFERENCES "UserProfiles" ("Id") ON DELETE CASCADE;
ALTER TABLE "CartItems" ADD CONSTRAINT "FK_CartItems_CartId" FOREIGN KEY ("CartId") REFERENCES "Carts" ("Id") ON DELETE CASCADE;
ALTER TABLE "CartItems" ADD CONSTRAINT "FK_CartItems_ProductId" FOREIGN KEY ("ProductId") REFERENCES "Products" ("Id") ON DELETE RESTRICT;
ALTER TABLE "CouponRedemptions" ADD CONSTRAINT "FK_CouponRedemptions_CouponId" FOREIGN KEY ("CouponId") REFERENCES "Coupons" ("Id") ON DELETE RESTRICT;
ALTER TABLE "CouponRedemptions" ADD CONSTRAINT "FK_CouponRedemptions_OrderId" FOREIGN KEY ("OrderId") REFERENCES "Orders" ("Id") ON DELETE CASCADE;
ALTER TABLE "CouponRedemptions" ADD CONSTRAINT "FK_CouponRedemptions_UserProfileId" FOREIGN KEY ("UserProfileId") REFERENCES "UserProfiles" ("Id") ON DELETE RESTRICT;
ALTER TABLE "Orders" ADD CONSTRAINT "FK_Orders_CouponId" FOREIGN KEY ("CouponId") REFERENCES "Coupons" ("Id") ON DELETE RESTRICT;
ALTER TABLE "Orders" ADD CONSTRAINT "FK_Orders_UserProfileId" FOREIGN KEY ("UserProfileId") REFERENCES "UserProfiles" ("Id") ON DELETE RESTRICT;
ALTER TABLE "OrderItems" ADD CONSTRAINT "FK_OrderItems_OrderId" FOREIGN KEY ("OrderId") REFERENCES "Orders" ("Id") ON DELETE CASCADE;
ALTER TABLE "Receipts" ADD CONSTRAINT "FK_Receipts_OrderId" FOREIGN KEY ("OrderId") REFERENCES "Orders" ("Id") ON DELETE CASCADE;
ALTER TABLE "TransparencyRecords" ADD CONSTRAINT "FK_TransparencyRecords_OrderId" FOREIGN KEY ("OrderId") REFERENCES "Orders" ("Id") ON DELETE CASCADE;
ALTER TABLE "WalletIdentities" ADD CONSTRAINT "FK_WalletIdentities_UserId" FOREIGN KEY ("UserId") REFERENCES "AspNetUsers" ("Id") ON DELETE CASCADE;

-- Seed catalog (mirrors SeedData.Configure; fixed GUIDs).
INSERT INTO "Products" ("Id", "Name", "Slug", "Description", "Price", "StockQuantity", "ImageUrl", "Category", "IsActive", "Origin", "RoastLevel", "Process", "CreatedAt", "UpdatedAt") VALUES
('11111111-1111-1111-1111-111111111111', 'House Espresso', 'house-espresso', 'A bright double shot with chocolate notes.', 3.50, 100, '/images/products/house-espresso.jpg', 'Beans', TRUE, 'Brazil', 'Dark', 'Natural', '2026-01-01T00:00:00Z', NULL),
('22222222-2222-2222-2222-222222222222', 'Vanilla Cloud Latte', 'vanilla-cloud-latte', 'Steamed milk, espresso, and a light vanilla finish.', 5.75, 80, '/images/products/vanilla-cloud-latte.jpg', 'Beans', TRUE, 'Colombia', 'Medium', 'Washed', '2026-01-01T00:00:00Z', NULL),
('33333333-3333-3333-3333-333333333333', 'Midnight Cold Brew', 'midnight-cold-brew', 'Slow-steeped cold brew with a smooth roasted profile.', 4.95, 60, '/images/products/midnight-cold-brew.jpg', 'Beans', TRUE, 'Ethiopia', 'Light', 'Honey', '2026-01-01T00:00:00Z', NULL);
