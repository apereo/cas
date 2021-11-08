package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.web.support.WebUtils;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletResponse;

/**
 * Helper class for the WebAuthn webflow actions.
 *
 * @author Jerome LELEU
 * @since 6.5.0
 */
@UtilityClass
public class WebAuthnActionHelper {

    /**
     * Add the Spring Security CSRF token to the webflow
     * (it mimics the CsrfFilter behavior).
     *
     * @param context the webflow request context
     * @param csrfTokenRepository the Spring Security CSRF tokens repository
     */
    public static void addCsrfTokenToFlowScope(final RequestContext context, final CsrfTokenRepository csrfTokenRepository) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        request.setAttribute(HttpServletResponse.class.getName(), response);

        var csrfToken = csrfTokenRepository.loadToken(request);
        if (csrfToken == null) {
            csrfToken = csrfTokenRepository.generateToken(request);
            csrfTokenRepository.saveToken(csrfToken, request, response);
        }
        context.getFlowScope().put(csrfToken.getParameterName(), csrfToken);
    }
}
