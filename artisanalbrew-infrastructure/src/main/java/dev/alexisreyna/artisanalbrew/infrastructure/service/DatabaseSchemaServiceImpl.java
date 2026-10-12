package dev.alexisreyna.artisanalbrew.infrastructure.service;

import dev.alexisreyna.artisanalbrew.application.dto.DatabaseSchemaDto;
import dev.alexisreyna.artisanalbrew.application.dto.SchemaColumnDto;
import dev.alexisreyna.artisanalbrew.application.dto.SchemaRelationshipDto;
import dev.alexisreyna.artisanalbrew.application.dto.SchemaTableDto;
import dev.alexisreyna.artisanalbrew.application.service.DatabaseSchemaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Port of {@code DatabaseSchemaService} ({@code Infrastructure/Services}).
 * Projects the live JPA metamodel into a structure-and-counts document:
 * never row contents. Identity tables are withheld, like .NET.
 */
@Service
public class DatabaseSchemaServiceImpl implements DatabaseSchemaService {
    private static final List<String> NOTES = List.of(
            "Structure and row counts only — no row contents are exposed through this endpoint.",
            "ASP.NET Identity tables (AspNetUsers, AspNetRoles, and friends) are withheld.",
            "Row counts are read live from the running database and cached for 60 seconds.");

    /** FK columns by entity, mirroring the V1 foreign keys (see spec relationships). */
    private static final Set<String> FOREIGN_KEYS = Set.of(
            "Cart.UserProfileId", "CartItem.CartId", "CartItem.ProductId",
            "CouponRedemption.CouponId", "CouponRedemption.UserProfileId", "CouponRedemption.OrderId",
            "Order.CouponId", "Order.UserProfileId", "OrderItem.OrderId",
            "TransparencyRecord.OrderId", "WalletIdentity.UserId");

    private volatile CachedSchema cached;

    @PersistenceContext
    private EntityManager entities;

    private final DataSource dataSource;

    public DatabaseSchemaServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DatabaseSchemaDto getSchema(boolean includeCounts) {
        CachedSchema hit = cached;
        if (hit != null && hit.key() == includeCounts
                && hit.computedAt().plusSeconds(60).isAfter(Instant.now())) {
            return hit.schema();
        }
        DatabaseSchemaDto schema = build(includeCounts);
        cached = new CachedSchema(includeCounts, Instant.now(), schema);
        return schema;
    }

    private record CachedSchema(boolean key, Instant computedAt, DatabaseSchemaDto schema) {
    }

    private DatabaseSchemaDto build(boolean includeCounts) {
        List<SchemaTableDto> tables = new ArrayList<>();
        for (EntityType<?> entity : entities.getMetamodel().getEntities()) {
            Class<?> javaType = entity.getJavaType();
            if (javaType == null || javaType.getPackageName().contains("identity")
                    || javaType.getSimpleName().equals("ApplicationUserEntity")
                    || javaType.getSimpleName().equals("RoleEntity")
                    || javaType.getSimpleName().equals("UserRoleEntity")) {
                continue;
            }
            Table table = javaType.getAnnotation(Table.class);
            String tableName = table == null ? javaType.getSimpleName() : unquote(table.name());
            List<SchemaColumnDto> columns = new ArrayList<>();
            for (Attribute<?, ?> attribute : entity.getAttributes()) {
                boolean primaryKey = attribute.getName().equals("id");
                columns.add(new SchemaColumnDto(
                        attribute.getName(),
                        attribute.getJavaType() == null ? "object" : attribute.getJavaType().getSimpleName(),
                        !primaryKey,
                        primaryKey,
                        FOREIGN_KEYS.contains(javaType.getSimpleName().replace("Entity", "") + "." + attribute.getName()),
                        null));
            }
            columns.sort(Comparator.comparing(SchemaColumnDto::name));
            Long rows = null;
            if (includeCounts) {
                rows = (Long) entities.createQuery(
                        "select count(e) from " + javaType.getSimpleName() + " e").getSingleResult();
            }
            List<SchemaRelationshipDto> references = columns.stream()
                    .filter(SchemaColumnDto::foreignKey)
                    .map(c -> new SchemaRelationshipDto(c.name(), "", "", true))
                    .toList();
            tables.add(new SchemaTableDto(
                    javaType.getSimpleName().replace("Entity", ""), tableName,
                    groupFor(javaType.getSimpleName().replace("Entity", "")),
                    rows, columns, references));
        }
        tables.sort(Comparator.comparing(SchemaTableDto::entity));
        int columnCount = tables.stream().mapToInt(t -> t.columns().size()).sum();
        int relationshipCount = tables.stream().mapToInt(t -> t.references().size()).sum();
        long totalRows = tables.stream().mapToLong(t -> t.rows() == null ? 0 : t.rows()).sum();
        return new DatabaseSchemaDto(Instant.now().toString(), provider(),
                tables.size(), columnCount, relationshipCount, totalRows, NOTES, tables);
    }

    private String provider() {
        try (var connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    static String unquote(String name) {
        if (name != null && name.length() >= 2 && name.startsWith("\"") && name.endsWith("\"")) {
            return name.substring(1, name.length() - 1);
        }
        return name;
    }

    /** Buckets tables into subsystems — mirrors .NET {@code GroupFor}. */
    static String groupFor(String entityName) {
        return switch (entityName) {
            case "Product", "Cart", "CartItem", "Order", "OrderItem", "Coupon", "CouponRedemption", "Receipt" ->
                    "Commerce";
            case "UserProfile", "WalletIdentity", "WalletAuthChallenge", "WalletStatusEvent", "SmartAccountRecord",
                    "ApplicationUser" -> "Identity & wallets";
            case "StakingLedgerEntry", "RewardClaim", "StakingReconciliationCheckpoint", "SolanaFaucetClaim" ->
                    "Staking";
            case "TransparencyRecord", "CrossChainSolverCheckpoint", "CrossChainSolverFill" -> "Settlement";
            default -> {
                if (entityName.startsWith("Agent")) {
                    yield "Agentic commerce";
                }
                if (entityName.startsWith("Sponsorship")) {
                    yield "Agentic commerce";
                }
                yield "Other";
            }
        };
    }
}
