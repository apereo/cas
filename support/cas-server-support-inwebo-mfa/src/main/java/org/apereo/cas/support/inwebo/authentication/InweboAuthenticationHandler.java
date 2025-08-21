package org.apereo.cas.support.inwebo.authentication;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.mfa.InweboMultifactorAuthenticationProperties;
import org.apereo.cas.support.inwebo.service.InweboService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

import javax.security.auth.login.FailedLoginException;

/**
 * The Inwebo authentication handler.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Slf4j
@Getter
public class InweboAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler implements MultifactorAuthenticationHandler {

    private final InweboService service;

    private final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider;

    public InweboAuthenticationHandler(
        final PrincipalFactory principalFactory,
        final InweboMultifactorAuthenticationProperties inweboProperties,
        final InweboService service,
        final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
        super(inweboProperties.getName(), principalFactory, inweboProperties.getOrder());
        this.service = service;
        this.multifactorAuthenticationProvider = multifactorAuthenticationProvider;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) throws Throwable {
        val inweboCredential = (InweboCredential) credential;
        val login = inweboCredential.getLogin();
        LOGGER.trace("Inwebo credential login is [{}]", login);

        val otp = inweboCredential.getOtp();
        var authenticated = inweboCredential.isAlreadyAuthenticated();
        var deviceName = inweboCredential.getDeviceName();
        if (StringUtils.isNotBlank(otp)) {
            val response = this.service.authenticateExtended(login, otp);
            if (response.isOk()) {
                authenticated = true;
                deviceName = response.getDeviceName();
            }
        }

        if (authenticated) {
            inweboCredential.setDeviceName(deviceName);
            LOGGER.info("Authenticated user: [{}] for device: [{}]", login, deviceName);
            val principal = this.principalFactory.createPrincipal(login);
            return createHandlerResult(inweboCredential, principal);
        }
        throw new FailedLoginException("Cannot validate authentication for: " + login);
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
