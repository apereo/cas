package org.apereo.cas.mfa.twilio;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationFailedException;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.mfa.twilio.CasTwilioMultifactorAuthenticationProperties;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link CasTwilioMultifactorAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@Getter
public class CasTwilioMultifactorAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler
    implements MultifactorAuthenticationHandler {

    private final CasTwilioMultifactorAuthenticationService casTwilioMultifactorAuthenticationService;

    private final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider;

    private final ConfigurableApplicationContext applicationContext;

    public CasTwilioMultifactorAuthenticationHandler(
        final CasTwilioMultifactorAuthenticationService casTwilioMultifactorAuthenticationService,
        final CasTwilioMultifactorAuthenticationProperties properties,
        final ConfigurableApplicationContext applicationContext,

        final PrincipalFactory principalFactory,
        final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
        super(properties.getName(), principalFactory, properties.getOrder());
        this.casTwilioMultifactorAuthenticationService = casTwilioMultifactorAuthenticationService;
        this.multifactorAuthenticationProvider = multifactorAuthenticationProvider;
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean supports(final Credential credential) {
        return CasTwilioMultifactorTokenCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return CasTwilioMultifactorTokenCredential.class.isAssignableFrom(clazz);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential,
                                                                    final Service service) throws Exception {
        return FunctionUtils.doAndThrow(() -> {
            val tokenCredential = (CasTwilioMultifactorTokenCredential) credential;
            val authentication = tokenCredential.getCredentialMetadata().getProperty(Authentication.class.getName(), Authentication.class);
            tokenCredential.getCredentialMetadata().removeProperty(Authentication.class.getName());
            val resolvedPrincipal = resolvePrincipal(applicationContext, authentication.getPrincipal());
            val principal = casTwilioMultifactorAuthenticationService.validate(resolvedPrincipal, tokenCredential);
            return createHandlerResult(tokenCredential, principal);
        }, MultifactorAuthenticationFailedException::new);
    }
}
