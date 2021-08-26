package org.apereo.cas.mfa.simple;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.util.LoggingUtils;
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
public class CasSimpleMultifactorAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler implements MultifactorAuthenticationHandler {
    private final CentralAuthenticationService centralAuthenticationService;

    public CasSimpleMultifactorAuthenticationHandler(final String name,
                                                     final ServicesManager servicesManager,
                                                     final PrincipalFactory principalFactory,
                                                     final CentralAuthenticationService centralAuthenticationService,
                                                     final Integer order) {
        super(name, servicesManager, principalFactory, order);
        this.centralAuthenticationService = centralAuthenticationService;
    }

    @Override
    public boolean supports(final Credential credential) {
        return CasSimpleMultifactorTokenCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return CasSimpleMultifactorTokenCredential.class.isAssignableFrom(clazz);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        val tokenCredential = (CasSimpleMultifactorTokenCredential) credential;
        val tokenId = CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator.normalize(tokenCredential.getId());
        LOGGER.debug("Received token [{}]", tokenId);

        val authentication = WebUtils.getInProgressAuthentication();
        val uid = authentication.getPrincipal().getId();

        try {
            LOGGER.debug("Received principal id [{}]. Attempting to locate token in registry...", uid);
            val acct = centralAuthenticationService.getTicket(tokenId, CasSimpleMultifactorAuthenticationTicket.class);
            val properties = acct.getProperties();
            if (!properties.containsKey(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL)) {
                LOGGER.warn("Unable to locate principal for token [{}]", tokenId);
                deleteToken(acct);
                throw new FailedLoginException("Failed to authenticate code " + tokenId);
            }
            val principal = Principal.class.cast(properties.get(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL));
            if (!principal.equals(authentication.getPrincipal())) {
                LOGGER.warn("Principal assigned to token [{}] is unauthorized for of token [{}]", principal.getId(), tokenId);
                deleteToken(acct);
                throw new FailedLoginException("Failed to authenticate code " + tokenId);
            }
            deleteToken(acct);

            LOGGER.debug("Validated token [{}] successfully for [{}].", tokenId, uid);
            return createHandlerResult(tokenCredential, this.principalFactory.createPrincipal(uid));
        } catch (final AbstractTicketException e) {
            LoggingUtils.error(LOGGER, e);
            throw new FailedLoginException(e.getMessage());
        }
    }

    /**
     * Delete token.
     *
     * @param acct the acct
     */
    protected void deleteToken(final CasSimpleMultifactorAuthenticationTicket acct) {
        this.centralAuthenticationService.deleteTicket(acct.getId());
    }
}
