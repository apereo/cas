package org.apereo.cas.qr.authentication;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.qr.validation.QRAuthenticationTokenValidatorService;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.Optional;

/**
 * This is {@link QRAuthenticationTokenAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class QRAuthenticationTokenAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final QRAuthenticationTokenValidatorService tokenValidatorService;

    public QRAuthenticationTokenAuthenticationHandler(final ServicesManager servicesManager,
        final PrincipalFactory principalFactory, final QRAuthenticationTokenValidatorService tokenValidatorService) {
        super(StringUtils.EMPTY, servicesManager, principalFactory, 0);
        this.tokenValidatorService = tokenValidatorService;
    }

    @Override
    public boolean supports(final Credential credential) {
        return QRAuthenticationTokenCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return QRAuthenticationTokenCredential.class.isAssignableFrom(clazz);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        val tokenCredential = (QRAuthenticationTokenCredential) credential;
        LOGGER.debug("Received token [{}]", tokenCredential.getId());
        val result = tokenValidatorService.validate(Optional.empty(), tokenCredential.getId());
        if (result.isSuccess()) {
            val principal = result.getAuthentication().getPrincipal();
            return createHandlerResult(tokenCredential, principal);
        }
        throw new FailedLoginException("Unable to verify QR code");
    }
}

