package org.apereo.cas.mfa.accepto.web.flow.qr;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.mfa.accepto.AccepttoEmailCredential;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link AccepttoQRCodeAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class AccepttoQRCodeAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    public AccepttoQRCodeAuthenticationHandler(final ServicesManager servicesManager,
                                               final PrincipalFactory principalFactory) {
        super(StringUtils.EMPTY, servicesManager, principalFactory, 0);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) {
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
