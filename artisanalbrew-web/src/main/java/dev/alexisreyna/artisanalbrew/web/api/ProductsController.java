package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.dto.CreateProductRequest;
import dev.alexisreyna.artisanalbrew.application.dto.ProductDto;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateProductRequest;
import dev.alexisreyna.artisanalbrew.application.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/** Port of {@code ProductsController} ({@code Web/Controllers}). */
@RestController
@RequestMapping("/api/products")
public class ProductsController {
    private final ProductService products;

    public ProductsController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    public List<ProductDto> all() {
        return products.getProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> byId(@PathVariable UUID id) {
        return products.getById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductDto> bySlug(@PathVariable String slug) {
        return products.getBySlug(slug).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestBody CreateProductRequest request) {
        ProductDto product = products.create(request);
        return ResponseEntity.created(URI.create("/api/products/" + product.id())).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable UUID id, @RequestBody UpdateProductRequest request) {
        return products.update(id, request).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return products.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
