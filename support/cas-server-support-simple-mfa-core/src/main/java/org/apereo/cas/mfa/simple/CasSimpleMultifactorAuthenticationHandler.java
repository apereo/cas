package org.apereo.cas.mfa.simple;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * This is {@link CasSimpleMultifactorAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Getter
public class CasSimpleMultifactorAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler
    implements MultifactorAuthenticationHandler {
    private final CasSimpleMultifactorAuthenticationService multifactorAuthenticationService;

    private final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider;

    public CasSimpleMultifactorAuthenticationHandler(final CasSimpleMultifactorAuthenticationProperties properties,
                                                     final ServicesManager servicesManager,
                                                     final PrincipalFactory principalFactory,
                                                     final CasSimpleMultifactorAuthenticationService mfaService,
                                                     final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
        super(properties.getName(), servicesManager, principalFactory, properties.getOrder());
        this.multifactorAuthenticationService = mfaService;
        this.multifactorAuthenticationProvider = multifactorAuthenticationProvider;
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
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) throws GeneralSecurityException {
        try {
            val tokenCredential = (CasSimpleMultifactorTokenCredential) credential;
            val authentication = WebUtils.getInProgressAuthentication();
            val principal = multifactorAuthenticationService.validate(authentication, tokenCredential);
            return createHandlerResult(tokenCredential, principal);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            throw new FailedLoginException(e.getMessage());
        }
    }
}
