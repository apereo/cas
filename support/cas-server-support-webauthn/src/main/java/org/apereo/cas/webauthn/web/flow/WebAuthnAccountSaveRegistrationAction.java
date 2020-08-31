package org.apereo.cas.webauthn.web.flow;

import demo.webauthn.RegistrationStorage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WebAuthnAccountSaveRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Getter
public class WebAuthnAccountSaveRegistrationAction extends AbstractAction {
    private final RegistrationStorage webAuthnCredentialRepository;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return success();
    }
}
