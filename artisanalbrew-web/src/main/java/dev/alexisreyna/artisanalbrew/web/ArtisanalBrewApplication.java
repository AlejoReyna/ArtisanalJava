package dev.alexisreyna.artisanalbrew.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Composition root (mirrors {@code Web/Program.cs}). Scans the web layer and
 * the infrastructure persistence/security/service packages; the application
 * and domain layers stay framework-free and are only consumed.
 */
@SpringBootApplication(scanBasePackages = {
        "dev.alexisreyna.artisanalbrew.web",
        "dev.alexisreyna.artisanalbrew.infrastructure"
})
public class ArtisanalBrewApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArtisanalBrewApplication.class, args);
    }
}
