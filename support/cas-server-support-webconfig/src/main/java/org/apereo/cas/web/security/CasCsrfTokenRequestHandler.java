package org.apereo.cas.web.security;

import module java.base;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link CasCsrfTokenRequestHandler}, the CSRF token handler of the endpoints that CAS protects against CSRF.
 * Views receive the token masked, as {@link XorCsrfTokenRequestAttributeHandler} renders it. A request may send the token
 * back masked, as views render it, or raw, as the {@code XSRF-TOKEN} cookie and the webflows that read the token repository
 * carry it. The token is loaded on every request to a protected endpoint, so its cookie is written before the view starts
 * writing the response; elsewhere it is loaded only when used.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class CasCsrfTokenRequestHandler implements CsrfTokenRequestHandler {
    private final XorCsrfTokenRequestAttributeHandler maskedTokenHandler = new XorCsrfTokenRequestAttributeHandler();

    private final XorCsrfTokenRequestAttributeHandler eagerMaskedTokenHandler = new XorCsrfTokenRequestAttributeHandler();

    private final RequestMatcher protectedEndpoints;

    public CasCsrfTokenRequestHandler(final RequestMatcher protectedEndpoints) {
        this.protectedEndpoints = protectedEndpoints;
        this.maskedTokenHandler.setSecureRandom(RandomUtils.getNativeInstance());
        this.eagerMaskedTokenHandler.setSecureRandom(RandomUtils.getNativeInstance());
        this.eagerMaskedTokenHandler.setCsrfRequestAttributeName(null);
    }

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final Supplier<CsrfToken> csrfToken) {
        val handler = protectedEndpoints.matches(request) ? eagerMaskedTokenHandler : maskedTokenHandler;
        handler.handle(request, response, csrfToken);
    }

    @Override
    public @Nullable String resolveCsrfTokenValue(final HttpServletRequest request, final CsrfToken csrfToken) {
        val maskedToken = maskedTokenHandler.resolveCsrfTokenValue(request, csrfToken);
        return maskedToken != null ? maskedToken : CsrfTokenRequestHandler.super.resolveCsrfTokenValue(request, csrfToken);
    }
}
