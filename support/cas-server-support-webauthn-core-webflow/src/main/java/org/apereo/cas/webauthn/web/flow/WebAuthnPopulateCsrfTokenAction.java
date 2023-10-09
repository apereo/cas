package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link WebAuthnPopulateCsrfTokenAction}.
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

            var csrfToken = csrfTokenRepository.loadToken(request);
            if (csrfToken == null) {
                csrfToken = csrfTokenRepository.generateToken(request);
                csrfTokenRepository.saveToken(csrfToken, request, response);
            }
            context.getFlowScope().put(csrfToken.getParameterName(), csrfToken);
        });
    }
}
