package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import com.yubico.webauthn.core.RegistrationStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WebAuthnAccountCheckRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class WebAuthnAccountCheckRegistrationAction extends AbstractAction {
    private final RegistrationStorage webAuthnCredentialRepository;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();
        LOGGER.trace("Checking registration record for [{}]", principal.getId());
        val registrations = webAuthnCredentialRepository.getRegistrationsByUsername(principal.getId());
        if (!registrations.isEmpty()) {
            return success();
        }
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
    }
}
