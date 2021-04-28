package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationCredential;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LoggingUtils;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
public class DuoSecurityAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler
    implements MultifactorAuthenticationHandler {
    @Getter
    private final DuoSecurityMultifactorAuthenticationProvider multifactorAuthenticationProvider;

    private final List<MultifactorAuthenticationPrincipalResolver> multifactorAuthenticationPrincipalResolver;

    public DuoSecurityAuthenticationHandler(final String name,
                                            final ServicesManager servicesManager,
                                            final PrincipalFactory principalFactory,
                                            final DuoSecurityMultifactorAuthenticationProvider multifactorAuthenticationProvider,
                                            final Integer order,
                                            final List<MultifactorAuthenticationPrincipalResolver> multifactorAuthenticationPrincipalResolver) {
        super(name, servicesManager, principalFactory, order);
        this.multifactorAuthenticationProvider = multifactorAuthenticationProvider;
        this.multifactorAuthenticationPrincipalResolver = multifactorAuthenticationPrincipalResolver;
    }

    @Override
    public boolean supports(final Credential credential) {
        if (credential instanceof MultifactorAuthenticationCredential) {
            val id = ((MultifactorAuthenticationCredential) credential).getProviderId();
            return multifactorAuthenticationProvider.matches(id);
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
     * @return the result of this handler
     * @throws GeneralSecurityException general security exception for errors
     */
    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        if (credential instanceof DuoSecurityPasscodeCredential) {
            LOGGER.debug("Attempting to authenticate credential via duo passcode");
            return authenticateDuoPasscodeCredential(credential);
        }
        if (credential instanceof DuoSecurityUniversalPromptCredential) {
            LOGGER.debug("Attempting to authenticate credential via duo universal prompt");
            return authenticateDuoUniversalPromptCredential(credential);
        }
        if (credential instanceof DuoSecurityDirectCredential) {
            LOGGER.debug("Attempting to directly authenticate credential against Duo");
            return authenticateDuoApiCredential(credential);
        }
        return authenticateDuoCredential(credential);
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
            .orElseThrow(() -> new IllegalStateException("Unable to resolve principal for multifactor authentication"));
    }

    @SneakyThrows
    private AuthenticationHandlerExecutionResult authenticateDuoPasscodeCredential(final Credential credential) {
        try {
            val duoAuthenticationService = multifactorAuthenticationProvider.getDuoAuthenticationService();
            val creds = DuoSecurityPasscodeCredential.class.cast(credential);
            if (duoAuthenticationService.authenticate(creds).isSuccess()) {
                val principal = principalFactory.createPrincipal(creds.getId());
                return createHandlerResult(credential, principal, new ArrayList<>(0));
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        throw new FailedLoginException("Duo passcode authentication has failed");
    }

    @SneakyThrows
    private AuthenticationHandlerExecutionResult authenticateDuoUniversalPromptCredential(final Credential c) {
        try {
            val duoAuthenticationService = multifactorAuthenticationProvider.getDuoAuthenticationService();
            val credential = (DuoSecurityUniversalPromptCredential) c;
            val result = duoAuthenticationService.authenticate(credential);
            if (result.isSuccess()) {
                val principal = principalFactory.createPrincipal(result.getUsername(), result.getAttributes());
                LOGGER.debug("Duo has successfully authenticated [{}]", principal.getId());
                return createHandlerResult(credential, principal, new ArrayList<>(0));
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        throw new FailedLoginException("Duo universal prompt authentication has failed");
    }

    private AuthenticationHandlerExecutionResult authenticateDuoApiCredential(final Credential credential) throws FailedLoginException {
        try {
            val duoAuthenticationService = multifactorAuthenticationProvider.getDuoAuthenticationService();
            val creds = DuoSecurityDirectCredential.class.cast(credential);
            if (duoAuthenticationService.authenticate(creds).isSuccess()) {
                val principal = resolvePrincipal(creds.getPrincipal());
                LOGGER.debug("Duo has successfully authenticated [{}]", principal.getId());
                return createHandlerResult(credential, principal, new ArrayList<>(0));
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        throw new FailedLoginException("Duo authentication has failed");
    }

    private AuthenticationHandlerExecutionResult authenticateDuoCredential(final Credential credential) throws FailedLoginException {
        try {
            val duoCredential = (DuoSecurityCredential) credential;
            if (!duoCredential.isValid()) {
                throw new GeneralSecurityException("Duo credential validation failed. Ensure a username "
                    + " and the signed Duo response is configured and passed. Credential received: " + duoCredential);
            }

            val duoAuthenticationService = multifactorAuthenticationProvider.getDuoAuthenticationService();
            val userId = duoAuthenticationService.authenticate(duoCredential).getUsername();
            LOGGER.debug("Verified Duo authentication for user [{}]", userId);
            val primaryCredentialsUsername = duoCredential.getUsername();

            val isGoodAuthentication = userId.equals(primaryCredentialsUsername);

            if (isGoodAuthentication) {
                LOGGER.info("Successful Duo authentication for [{}]", primaryCredentialsUsername);
                val principal = this.principalFactory.createPrincipal(userId);
                return createHandlerResult(credential, principal, new ArrayList<>(0));
            }
            throw new FailedLoginException("Duo authentication username "
                + primaryCredentialsUsername + " does not match Duo response: " + userId);

        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            throw new FailedLoginException(e.getMessage());
        }
    }
}
