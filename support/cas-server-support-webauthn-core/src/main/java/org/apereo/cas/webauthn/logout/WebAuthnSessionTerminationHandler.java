package org.apereo.cas.webauthn.logout;

import org.apereo.cas.logout.SessionTerminationHandler;
import org.apereo.cas.web.support.WebUtils;
import com.yubico.core.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.webflow.execution.RequestContext;
import java.io.Serializable;
import java.util.List;

/**
 * This is {@link WebAuthnSessionTerminationHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
public class WebAuthnSessionTerminationHandler implements SessionTerminationHandler {
    protected final SessionManager sessionManager;
    protected final CsrfTokenRepository webAuthnCsrfTokenRepository;
    
    @Override
    public List<? extends Serializable> beforeSessionTermination(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        webAuthnCsrfTokenRepository.saveToken(null, request, response);
        return SessionTerminationHandler.super.beforeSessionTermination(requestContext);
    }
}
