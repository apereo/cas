package org.apereo.cas.webauthn.web.flow;

import com.yubico.webauthn.RegistrationStorage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WebAuthnAccountSaveRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Getter
public class WebAuthnAccountSaveRegistrationAction extends AbstractAction {
    private final RegistrationStorage webAuthnCredentialRepository;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return success();
    }
}
