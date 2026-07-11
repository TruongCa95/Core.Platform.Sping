package vn.aequitas.coreplatform.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Opt-in API-key gate, the counterpart of the .NET {@code ApiKeyMiddleware}.
 * Enforced only when {@code app.api-key} is configured; otherwise a pass-through.
 * The Swagger UI and OpenAPI document are always allowed.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-Api-Key";

    private final String configuredKey;

    public ApiKeyFilter(@Value("${app.api-key:}") String configuredKey) {
        this.configuredKey = configuredKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // No key configured -> feature disabled, let everything through.
        if (!StringUtils.hasText(configuredKey)) {
            chain.doFilter(request, response);
            return;
        }

        // Allow Swagger UI and the OpenAPI document without a key.
        String path = request.getRequestURI();
        if (path != null && (path.startsWith("/swagger") || path.startsWith("/v3/api-docs"))) {
            chain.doFilter(request, response);
            return;
        }

        String provided = request.getHeader(HEADER_NAME);
        if (provided == null || !provided.equals(configuredKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing or invalid API key.\",\"statusCode\":401}");
            return;
        }

        chain.doFilter(request, response);
    }
}
