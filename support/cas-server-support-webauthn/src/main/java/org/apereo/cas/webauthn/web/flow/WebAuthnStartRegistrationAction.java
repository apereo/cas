package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;

import com.yubico.webauthn.storage.RegistrationStorage;
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

    /**
     * Attribute name that points to the web application id put into the webflow.
     */
    public static final String FLOW_SCOPE_WEB_AUTHN_APPLICATION_ID = "webauthnApplicationId";

    private final RegistrationStorage webAuthnCredentialRepository;

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        requestContext.getFlowScope().put(FLOW_SCOPE_WEB_AUTHN_APPLICATION_ID,
            casProperties.getAuthn().getMfa().getWebAuthn().getApplicationId());
        return null;
    }
}
