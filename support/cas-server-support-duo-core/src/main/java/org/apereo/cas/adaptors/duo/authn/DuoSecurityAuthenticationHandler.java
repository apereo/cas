package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationCredential;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LoggingUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Authenticate CAS credentials against Duo Security.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@Slf4j
@Monitorable
public class DuoSecurityAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler
    implements MultifactorAuthenticationHandler {
    @Getter
    private final ObjectProvider<? extends DuoSecurityMultifactorAuthenticationProvider> multifactorAuthenticationProvider;

    private final List<MultifactorAuthenticationPrincipalResolver> multifactorAuthenticationPrincipalResolver;

    public DuoSecurityAuthenticationHandler(final String name,
                                            final ServicesManager servicesManager,
                                            final PrincipalFactory principalFactory,
                                            final ObjectProvider<DuoSecurityMultifactorAuthenticationProvider> multifactorAuthenticationProvider,
                                            final Integer order,
                                            final List<MultifactorAuthenticationPrincipalResolver> multifactorAuthenticationPrincipalResolver) {
        super(name, servicesManager, principalFactory, order);
        this.multifactorAuthenticationProvider = multifactorAuthenticationProvider;
        this.multifactorAuthenticationPrincipalResolver = multifactorAuthenticationPrincipalResolver;
    }

    @Override
    public boolean supports(final Credential credential) {
        if (credential instanceof final MultifactorAuthenticationCredential mfaCredential) {
            val id = mfaCredential.getProviderId();
            return multifactorAuthenticationProvider.getObject().matches(id);
        }
        return false;
    }

    /**
     * Do an out of band request using the DuoWeb api (encapsulated in DuoSecurityAuthenticationService)
     * to the hosted duo service. If it is successful
     * it will return a String containing the username of the successfully authenticated user, but if not - will
     * return a blank String or null.
     *
     * @param credential Credential to authenticate.
     * @param service the requesting service, if any.
     * @return the result of this handler
     * @throws GeneralSecurityException general security exception for errors
     */
    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) throws Exception {
        if (credential instanceof DuoSecurityPasscodeCredential) {
            LOGGER.debug("Attempting to authenticate credential via Duo Security passcode");
            return authenticateDuoPasscodeCredential(credential);
        }
        if (credential instanceof DuoSecurityUniversalPromptCredential) {
            LOGGER.debug("Attempting to authenticate credential via Duo Security universal prompt");
            return authenticateDuoUniversalPromptCredential(credential);
        }
        if (credential instanceof DuoSecurityDirectCredential) {
            LOGGER.debug("Attempting to directly authenticate credential against Duo");
            return authenticateDuoApiCredential(credential);
        }
        throw new FailedLoginException("Unknown Duo Security authentication attempt");
    }

    /**
     * Resolve principal.
     *
     * @param principal the principal
     * @return the principal
     */
    protected Principal resolvePrincipal(final Principal principal) {
        return multifactorAuthenticationPrincipalResolver
            .stream()
            .filter(resolver -> resolver.supports(principal))
            .findFirst()
            .map(r -> r.resolve(principal))
            .orElseThrow(() -> new IllegalStateException("Unable to resolve principal for Duo Security multifactor authentication"));
    }
    private AuthenticationHandlerExecutionResult authenticateDuoPasscodeCredential(final Credential credential) throws Exception {
        try {
            val duoAuthenticationService = multifactorAuthenticationProvider.getObject().getDuoAuthenticationService();
            val creds = (DuoSecurityPasscodeCredential) credential;
            if (duoAuthenticationService.authenticate(creds).isSuccess()) {
                val principal = principalFactory.createPrincipal(creds.getId());
                return createHandlerResult(credential, principal, new ArrayList<>(0));
            }
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        throw new FailedLoginException("Duo Security passcode authentication has failed");
    }

    private AuthenticationHandlerExecutionResult authenticateDuoUniversalPromptCredential(final Credential givenCredential) throws Exception {
        try {
            val duoAuthenticationService = multifactorAuthenticationProvider.getObject().getDuoAuthenticationService();
            val credential = (DuoSecurityUniversalPromptCredential) givenCredential;
            val result = duoAuthenticationService.authenticate(credential);
            if (result.isSuccess()) {
                val principal = principalFactory.createPrincipal(result.getUsername(), result.getAttributes());
                LOGGER.debug("Duo Security has successfully authenticated [{}]", principal.getId());
                return createHandlerResult(credential, principal, new ArrayList<>(0));
            }
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        throw new FailedLoginException("Duo Security universal prompt authentication has failed");
    }

    private AuthenticationHandlerExecutionResult authenticateDuoApiCredential(final Credential credential) throws FailedLoginException {
        try {
            val duoAuthenticationService = multifactorAuthenticationProvider.getObject().getDuoAuthenticationService();
            val creds = (DuoSecurityDirectCredential) credential;
            if (duoAuthenticationService.authenticate(creds).isSuccess()) {
                val principal = resolvePrincipal(creds.getPrincipal());
                LOGGER.debug("Duo Security has successfully authenticated [{}]", principal.getId());
                return createHandlerResult(credential, principal, new ArrayList<>(0));
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        throw new FailedLoginException("Duo Security authentication has failed");
    }
}
