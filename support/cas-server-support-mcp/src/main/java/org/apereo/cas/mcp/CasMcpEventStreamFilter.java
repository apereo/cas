package org.apereo.cas.mcp;

import module java.base;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link CasMcpEventStreamFilter}. It answers {@code GET} requests to the MCP endpoint with
 * {@code 405 Method Not Allowed}, which the Streamable HTTP transport defines as the way a server says it offers no
 * stream of server-initiated messages. CAS sends no messages outside the responses to client requests, and the
 * transport would otherwise keep the request open without sending a status or headers until a first event that never
 * comes, so clients that wait for those headers before completing initialization never connect.
 * <p>
 * It runs after authorization, so a request without valid credentials is still challenged first.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasMcpEventStreamFilter extends OncePerRequestFilter {
    /**
     * The methods the MCP endpoint accepts.
     */
    public static final String ALLOWED_METHODS = HttpMethod.POST.name() + ", " + HttpMethod.DELETE.name();

    private final RequestMatcher endpoint;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain chain)
            throws ServletException, IOException {
        if (endpoint.matches(request)) {
            response.setHeader(HttpHeaders.ALLOW, ALLOWED_METHODS);
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
            return;
        }
        chain.doFilter(request, response);
    }
}
