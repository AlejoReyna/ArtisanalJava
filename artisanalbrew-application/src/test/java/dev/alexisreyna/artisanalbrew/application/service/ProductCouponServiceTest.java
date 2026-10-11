package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.CartItemDto;
import dev.alexisreyna.artisanalbrew.application.dto.CreateCouponRequest;
import dev.alexisreyna.artisanalbrew.application.dto.CreateProductRequest;
import dev.alexisreyna.artisanalbrew.application.repository.CouponRepository;
import dev.alexisreyna.artisanalbrew.application.repository.ProductRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.Coupon;
import dev.alexisreyna.artisanalbrew.domain.entities.Product;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductCouponServiceTest {

    static final class FakeProducts implements ProductRepository {
        final Map<UUID, Product> store = new HashMap<>();

        @Override
        public List<Product> getProducts() {
            return new ArrayList<>(store.values());
        }

        @Override
        public Optional<Product> getById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Product> getBySlug(String slug) {
            return store.values().stream().filter(p -> p.getSlug().equals(slug)).findFirst();
        }

        @Override
        public void add(Product product) {
            store.put(product.getId(), product);
        }

        @Override
        public void update(Product product) {
            store.put(product.getId(), product);
        }

        @Override
        public void delete(Product product) {
            store.remove(product.getId());
        }
    }

    static final class FakeCoupons implements CouponRepository {
        final Map<UUID, Coupon> store = new HashMap<>();

        @Override
        public List<Coupon> getCoupons() {
            return new ArrayList<>(store.values());
        }

        @Override
        public Optional<Coupon> getById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Coupon> getByNormalizedCode(String normalizedCode) {
            return store.values().stream().filter(c -> c.getNormalizedCode().equals(normalizedCode)).findFirst();
        }

        @Override
        public boolean hasUserRedeemed(UUID couponId, UUID userProfileId) {
            return false;
        }

        @Override
        public void add(Coupon coupon) {
            store.put(coupon.getId(), coupon);
        }

        @Override
        public void update(Coupon coupon) {
            store.put(coupon.getId(), coupon);
        }

        @Override
        public void delete(Coupon coupon) {
            store.remove(coupon.getId());
        }
    }

    private static CreateProductRequest productRequest(String name) {
        return new CreateProductRequest(name, "Tasty coffee", new BigDecimal("12.50"), 10,
                null, ProductCategory.BEANS, "Colombia", null, null);
    }

    @Test
    void productCrudAndSlugLookup() {
        DefaultProductService service = new DefaultProductService(new FakeProducts());
        var created = service.create(productRequest("Cold Brew"));
        assertThat(created.slug()).isEqualTo("cold-brew");
        assertThat(service.getBySlug("cold-brew")).isPresent();
        assertThat(service.update(created.id(), new dev.alexisreyna.artisanalbrew.application.dto.UpdateProductRequest(
                "Cold Brew", "Tasty coffee", new BigDecimal("13.00"), 5, null,
                ProductCategory.BEANS, "Colombia", null, null, true))).isPresent();
        assertThat(service.getById(created.id()).orElseThrow().price()).isEqualByComparingTo("13.00");
        assertThat(service.delete(created.id())).isTrue();
        assertThat(service.delete(created.id())).isFalse();
    }

    @Test
    void couponDuplicateAndQuote() {
        DefaultCouponService service =
                new DefaultCouponService(new FakeCoupons(), new DefaultOrderPricingService());
        var created = service.create(new CreateCouponRequest("SAVE10", new BigDecimal("10"), new BigDecimal("5")));
        assertThat(created.code()).isEqualTo("SAVE10");
        assertThatThrownBy(() -> service.create(new CreateCouponRequest("save10", new BigDecimal("5"), BigDecimal.ZERO)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");
        var items = List.of(new CartItemDto(UUID.randomUUID(), "Espresso", 2, new BigDecimal("10.00")));
        var quote = service.quote(UUID.randomUUID(), items, "save10");
        assertThat(quote.coupon().code()).isEqualTo("SAVE10");
        assertThat(quote.pricing().discountAmount()).isPositive();
    }
}
