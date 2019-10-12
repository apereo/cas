package org.apereo.cas.mfa.simple;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * This is {@link CasSimpleMultifactorAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class CasSimpleMultifactorAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private final TicketRegistry ticketRegistry;

    public CasSimpleMultifactorAuthenticationHandler(final String name,
                                                     final ServicesManager servicesManager,
                                                     final PrincipalFactory principalFactory,
                                                     final TicketRegistry ticketRegistry,
                                                     final Integer order) {
        super(name, servicesManager, principalFactory, order);
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        val tokenCredential = (CasSimpleMultifactorTokenCredential) credential;
        LOGGER.debug("Received token [{}]", tokenCredential.getId());

        val authentication = WebUtils.getInProgressAuthentication();
        val uid = authentication.getPrincipal().getId();

        LOGGER.debug("Received principal id [{}]. Attempting to locate token in registry...", uid);
        val acct = this.ticketRegistry.getTicket(tokenCredential.getId());

        if (acct == null) {
            LOGGER.warn("Authorization of token [{}] has failed. Token is not found in registry", tokenCredential.getId());
            throw new FailedLoginException("Failed to authenticate code " + tokenCredential.getId());
        }
        if (acct.isExpired()) {
            LOGGER.warn("Authorization of token [{}] has failed. Token found in registry has expired", tokenCredential.getId());
            this.ticketRegistry.deleteTicket(acct.getId());
            throw new FailedLoginException("Failed to authenticate code " + tokenCredential.getId());
        }

        this.ticketRegistry.deleteTicket(acct.getId());

        LOGGER.debug("Validated token [{}] successfully for [{}]. Creating authentication result and building principal...", tokenCredential.getId(), uid);
        return createHandlerResult(tokenCredential, this.principalFactory.createPrincipal(uid));
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return CasSimpleMultifactorTokenCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        return CasSimpleMultifactorTokenCredential.class.isAssignableFrom(credential.getClass());
    }
}
