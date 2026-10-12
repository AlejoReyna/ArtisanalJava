package dev.alexisreyna.artisanalbrew.web.chain;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * Port of {@code SelectedChainAccessor} ({@code Web/Services/Blockchain}).
 * Resolution order is preserved: {@code ?chainKey} query, then the
 * {@code ThisCafeteria.SelectedChain} cookie, then the registry default.
 * The cookie write skips committed responses, like the .NET
 * {@code Response.HasStarted} guard (Blazor circuits no longer apply, but
 * async dispatches do).
 */
@Component
public class SelectedChainAccessor {
    public static final String COOKIE_NAME = "ThisCafeteria.SelectedChain";

    private final ChainRegistry registry;

    public SelectedChainAccessor(ChainRegistry registry) {
        this.registry = registry;
    }

    public String selectedChainKey() {
        HttpServletRequest request = currentRequest();
        String query = request == null ? null : request.getParameter("chainKey");
        String cookie = request == null ? null : cookieValue(request);
        String candidate = query != null ? query : cookie != null ? cookie : registry.defaultChainKey();
        return registry.tryGet(candidate).filter(ChainDefinition::isEnabled)
                .map(ChainDefinition::getKey)
                .orElse(registry.defaultChainKey());
    }

    public ChainDefinition selectedChain() {
        return registry.getRequired(selectedChainKey());
    }

    public boolean select(String chainKey) {
        String key = chainKey == null ? "" : chainKey.trim();
        var definition = registry.tryGet(key).filter(ChainDefinition::isEnabled);
        if (definition.isEmpty()) {
            return false;
        }
        HttpServletResponse response = currentResponse();
        if (response != null && !response.isCommitted()) {
            Cookie cookie = new Cookie(COOKIE_NAME, definition.get().getKey());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge((int) Duration.ofDays(30).getSeconds());
            cookie.setSecure(currentRequest() != null && currentRequest().isSecure());
            cookie.setAttribute("SameSite", "Strict");
            response.addCookie(cookie);
        }
        return true;
    }

    private static String cookieValue(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static HttpServletRequest currentRequest() {
        var attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servlet ? servlet.getRequest() : null;
    }

    private static HttpServletResponse currentResponse() {
        var attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servlet ? servlet.getResponse() : null;
    }
}
