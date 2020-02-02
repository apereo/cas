package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;

import com.yubico.webauthn.RegistrationStorage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WebAuthnStartRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Getter
public class WebAuthnStartRegistrationAction extends AbstractAction {
    private final RegistrationStorage webAuthnCredentialRepository;
    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        requestContext.getFlowScope().put("webauthnApplicationId",
            casProperties.getAuthn().getMfa().getWebAuthn().getApplicationId());
        return null;
    }
}
