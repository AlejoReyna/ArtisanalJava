package dev.alexisreyna.artisanalbrew.web.support;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mirrors the {@code "sensitive"} rate limiter (30 requests/minute per IP,
 * HTTP 429) on the same controller surface. Single-instance fixed windows;
 * production behind multiple replicas wants a shared bucket store.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final List<String> SENSITIVE_PREFIXES =
            List.of("/api/orders", "/rewards", "/api/wallet-status", "/api/wallet-auth");
    private static final int LIMIT = 30;
    private static final long WINDOW_MILLIS = 60_000L;

    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return SENSITIVE_PREFIXES.stream().noneMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        Deque<Long> window = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        boolean allowed;
        synchronized (window) {
            while (!window.isEmpty() && window.peekFirst() + WINDOW_MILLIS < now) {
                window.pollFirst();
            }
            allowed = window.size() < LIMIT;
            if (allowed) {
                window.addLast(now);
            }
        }
        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return;
        }
        chain.doFilter(request, response);
    }
}
