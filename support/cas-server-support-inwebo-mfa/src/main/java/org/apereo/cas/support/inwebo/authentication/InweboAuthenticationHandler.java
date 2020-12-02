package org.apereo.cas.support.inwebo.authentication;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.mfa.InweboMultifactorProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * The Inwebo authentication handler.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Slf4j
public class InweboAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    public InweboAuthenticationHandler(final ServicesManager servicesManager,
                                       final PrincipalFactory principalFactory,
                                       final InweboMultifactorProperties inweboProperties) {
        super(inweboProperties.getName(),
              servicesManager,
              principalFactory,
              inweboProperties.getOrder());
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) {
        val login = credential.getId();
        LOGGER.info("Creating principal result for: {}", login);
        val pushCredential = (InweboCredential) credential;
        val principal = this.principalFactory.createPrincipal(login);
        return createHandlerResult(pushCredential, principal);
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return InweboCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        return InweboCredential.class.isAssignableFrom(credential.getClass());
    }
}
