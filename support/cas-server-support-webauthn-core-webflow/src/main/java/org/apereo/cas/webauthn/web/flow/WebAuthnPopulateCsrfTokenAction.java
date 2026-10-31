package org.apereo.cas.webauthn.web.flow;

import module java.base;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link WebAuthnPopulateCsrfTokenAction}. It puts the CSRF token of the request into the flow scope. The token is
 * the one that Spring Security's CSRF filter keeps for the request, when the filter ran, so the page and the filter agree on
 * a single token even when the request carries no token cookie yet; otherwise it is loaded from, or generated and saved to,
 * the token repository.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class WebAuthnPopulateCsrfTokenAction extends ConsumerExecutionAction {

    public WebAuthnPopulateCsrfTokenAction(final CsrfTokenRepository csrfTokenRepository) {
        super(context -> {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
            request.setAttribute(HttpServletResponse.class.getName(), response);
            val csrfToken = resolveCsrfToken(csrfTokenRepository, request, response);
            context.getFlowScope().put(csrfToken.getParameterName(), csrfToken);
        });
    }

    private static CsrfToken resolveCsrfToken(final CsrfTokenRepository csrfTokenRepository,
                                              final HttpServletRequest request,
                                              final HttpServletResponse response) {
        if (request.getAttribute(DeferredCsrfToken.class.getName()) instanceof final DeferredCsrfToken deferredCsrfToken) {
            return deferredCsrfToken.get();
        }
        val csrfToken = csrfTokenRepository.loadToken(request);
        if (csrfToken != null) {
            return csrfToken;
        }
        val generatedToken = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(generatedToken, request, response);
        return generatedToken;
    }
}
