package org.apereo.cas.mfa.simple;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationFailedException;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Objects;

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

    private final ConfigurableApplicationContext applicationContext;

    public CasSimpleMultifactorAuthenticationHandler(
        final CasSimpleMultifactorAuthenticationProperties properties,
        final ConfigurableApplicationContext applicationContext,
        final ServicesManager servicesManager,
        final PrincipalFactory principalFactory,
        final CasSimpleMultifactorAuthenticationService mfaService,
        final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
        super(properties.getName(), servicesManager, principalFactory, properties.getOrder());
        this.multifactorAuthenticationService = mfaService;
        this.multifactorAuthenticationProvider = multifactorAuthenticationProvider;
        this.applicationContext = applicationContext;
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
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential,
                                                                    final Service service) throws Exception {
        return FunctionUtils.doAndThrow(() -> {
            val tokenCredential = (CasSimpleMultifactorTokenCredential) credential;
            val credentialPrincipal = multifactorAuthenticationService.fetch(tokenCredential);
            val resolvedPrincipal = resolvePrincipal(applicationContext, credentialPrincipal);
            val principal = multifactorAuthenticationService.validate(resolvedPrincipal, tokenCredential);

            val activePrincipal = findActivePrincipal();
            if (!principal.equals(activePrincipal)) {
                LOGGER.warn("Credential principal [{}] does not match authentication principal [{}]",
                    principal.getId(), activePrincipal.getId());
                throw new MultifactorAuthenticationFailedException("Failed to authenticate code " + tokenCredential.getId());
            }
            return createHandlerResult(tokenCredential, principal);
        }, MultifactorAuthenticationFailedException::new);
    }

    protected Principal findActivePrincipal() {
        val authentication = Objects.requireNonNull(WebUtils.getInProgressAuthentication());
        val principal = authentication.getPrincipal();
        return principal.getOwner();
    }
}
