package org.apereo.cas.webauthn.web.flow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WebAuthnPreparePrimaryLoginAction}.
 *
 * @author Jerome LELEU
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Getter
public class WebAuthnPreparePrimaryLoginAction extends AbstractAction {
    private final CsrfTokenRepository csrfTokenRepository;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        WebAuthnActionHelper.addCsrfTokenToFlowScope(requestContext, csrfTokenRepository);
        return null;
    }
}
