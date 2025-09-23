package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.WebAuthnMultifactorAuthenticationProvider;

import com.yubico.core.RegistrationStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
public class WebAuthnAccountCheckRegistrationAction extends AbstractMultifactorAuthenticationAction<WebAuthnMultifactorAuthenticationProvider> {
    protected final RegistrationStorage webAuthnCredentialRepository;
    protected final TenantExtractor tenantExtractor;
    
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
        LOGGER.trace("Checking registration record for [{}]", principal.getId());
        val registrations = webAuthnCredentialRepository.getRegistrationsByUsername(principal.getId());
        if (!registrations.isEmpty()) {
            return success();
        }
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
    }
}
