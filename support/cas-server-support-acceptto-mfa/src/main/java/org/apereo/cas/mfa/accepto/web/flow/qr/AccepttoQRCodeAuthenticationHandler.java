package org.apereo.cas.mfa.accepto.web.flow.qr;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mfa.accepto.AccepttoEmailCredential;
import org.apereo.cas.services.ServicesManager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link AccepttoQRCodeAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Getter
public class AccepttoQRCodeAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler implements MultifactorAuthenticationHandler {

    private final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider;

    public AccepttoQRCodeAuthenticationHandler(final ServicesManager servicesManager,
                                               final PrincipalFactory principalFactory,
                                               final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
        super(StringUtils.EMPTY, servicesManager, principalFactory, 0);
        this.multifactorAuthenticationProvider = multifactorAuthenticationProvider;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) {
        val tokenCredential = (AccepttoEmailCredential) credential;
        LOGGER.debug("Received token [{}]", tokenCredential.getId());
        val principal = this.principalFactory.createPrincipal(tokenCredential.getId());
        return createHandlerResult(tokenCredential, principal);
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return AccepttoEmailCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        return AccepttoEmailCredential.class.isAssignableFrom(credential.getClass());
    }
}
