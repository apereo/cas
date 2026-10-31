package org.apereo.cas.mcp.oauth;

import module java.base;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * This is {@link CasMcpAuthenticationEntryPoint}. It answers unauthenticated requests to the MCP endpoint with two
 * challenges: {@code Bearer}, which points OAuth clients to the protected resource metadata and the required scope, and
 * the {@code Basic} challenge of Spring Security, for clients configured with a user name and password.
 * <p>
 * Being registered first, Spring Security also uses it as the default entry point for requests that no other entry
 * point claims, so any other request gets the {@code Basic} challenge alone, exactly as the Basic entry point answers.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class CasMcpAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /**
     * The realm of the Basic challenge, the Spring Security default.
     */
    public static final String BASIC_REALM = "Realm";

    private final RequestMatcher endpoint;

    private final CasMcpProtectedResource protectedResource;

    private final BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();

    public CasMcpAuthenticationEntryPoint(final RequestMatcher endpoint, final CasMcpProtectedResource protectedResource) {
        this.endpoint = endpoint;
        this.protectedResource = protectedResource;
        this.basicAuthenticationEntryPoint.setRealmName(BASIC_REALM);
    }

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response,
                         final AuthenticationException authException)
            throws IOException, ServletException {
        if (!endpoint.matches(request)) {
            basicAuthenticationEntryPoint.commence(request, response, authException);
            return;
        }
        response.addHeader(HttpHeaders.WWW_AUTHENTICATE, protectedResource.challenge(null, null));
        val challenges = new HttpServletResponseWrapper(response) {
            @Override
            public void setHeader(final String name, final String value) {
                if (Strings.CI.equals(name, HttpHeaders.WWW_AUTHENTICATE)) {
                    super.addHeader(name, value);
                } else {
                    super.setHeader(name, value);
                }
            }
        };
        basicAuthenticationEntryPoint.commence(request, challenges, authException);
    }
}
