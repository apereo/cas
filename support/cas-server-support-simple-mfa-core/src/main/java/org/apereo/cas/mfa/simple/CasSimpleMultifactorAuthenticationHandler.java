package org.apereo.cas.mfa.simple;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TransientSessionTicket;
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
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        val tokenCredential = (CasSimpleMultifactorTokenCredential) credential;
        LOGGER.debug("Received token [{}]", tokenCredential.getId());

        val authentication = WebUtils.getInProgressAuthentication();
        val uid = authentication.getPrincipal().getId();

        LOGGER.debug("Received principal id [{}]. Attempting to locate token in registry...", uid);
        val acct = this.centralAuthenticationService.getTicket(tokenCredential.getId(), TransientSessionTicket.class);

        if (acct == null) {
            LOGGER.warn("Authorization of token [{}] has failed. Token is not found in registry", tokenCredential.getId());
            throw new FailedLoginException("Failed to authenticate code " + tokenCredential.getId());
        }
        val properties = acct.getProperties();
        if (!properties.containsKey(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL)) {
            LOGGER.warn("Unable to locate principal for token [{}]", tokenCredential.getId());
            deleteToken(acct);
            throw new FailedLoginException("Failed to authenticate code " + tokenCredential.getId());
        }
        val principal = Principal.class.cast(properties.get(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL));
        if (!principal.equals(authentication.getPrincipal())) {
            LOGGER.warn("Principal assigned to token [{}] is unauthorized for of token [{}]", principal.getId(), tokenCredential.getId());
            deleteToken(acct);
            throw new FailedLoginException("Failed to authenticate code " + tokenCredential.getId());
        }
        if (acct.isExpired()) {
            LOGGER.warn("Authorization of token [{}] has failed. Token found in registry has expired", tokenCredential.getId());
            deleteToken(acct);
            throw new FailedLoginException("Failed to authenticate code " + tokenCredential.getId());
        }
        deleteToken(acct);

        LOGGER.debug("Validated token [{}] successfully for [{}]. Creating authentication result and building principal...", tokenCredential.getId(), uid);
        return createHandlerResult(tokenCredential, this.principalFactory.createPrincipal(uid));
    }

    /**
     * Delete token.
     *
     * @param acct the acct
     */
    protected void deleteToken(final TransientSessionTicket acct) {
        this.centralAuthenticationService.deleteTicket(acct.getId());
    }

    @Override
    public boolean supports(final Credential credential) {
        return CasSimpleMultifactorTokenCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return CasSimpleMultifactorTokenCredential.class.isAssignableFrom(clazz);
    }
}
