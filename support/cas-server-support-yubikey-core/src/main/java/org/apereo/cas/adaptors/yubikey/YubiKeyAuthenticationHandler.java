package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.adaptors.yubikey.registry.OpenYubiKeyAccountRegistry;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import com.yubico.client.v2.ResponseStatus;
import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.exceptions.YubicoValidationFailure;
import com.yubico.client.v2.exceptions.YubicoVerificationException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * An authentication handler that uses the Yubico cloud validation
 * platform to authenticate one-time password tokens that are
 * issued by a YubiKey device. To use YubiCloud you need a
 * client id and an API key which must be obtained from Yubico.
 * <p>For more info, please visit
 * <a href="http://yubico.github.io/yubico-java-client/">this link</a></p>
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@Getter
public class YubiKeyAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private final YubiKeyAccountRegistry registry;
    private final YubicoClient client;

    public YubiKeyAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                        final PrincipalFactory principalFactory,
                                        final YubicoClient client,
                                        final YubiKeyAccountRegistry registry,
                                        final Integer order) {
        super(name, servicesManager, principalFactory, order);
        this.registry = registry;
        this.client = client;
    }

    public YubiKeyAuthenticationHandler(final YubicoClient client) {
        this(StringUtils.EMPTY, null, null,
            client, new OpenYubiKeyAccountRegistry(new AcceptAllYubiKeyAccountValidator()), null);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        val yubiKeyCredential = (YubiKeyCredential) credential;

        val otp = yubiKeyCredential.getToken();

        if (!YubicoClient.isValidOTPFormat(otp)) {
            LOGGER.debug("Invalid OTP format [{}]", otp);
            throw new AccountNotFoundException("OTP format is invalid");
        }

        val authentication = WebUtils.getInProgressAuthentication();
        if (authentication == null) {
            throw new IllegalArgumentException("CAS has no reference to an authentication event to locate a principal");
        }
        val principal = authentication.getPrincipal();
        val uid = principal.getId();
        val publicId = registry.getAccountValidator().getTokenPublicId(otp);
        if (!this.registry.isYubiKeyRegisteredFor(uid, publicId)) {
            LOGGER.debug("YubiKey public id [{}] is not registered for user [{}]", publicId, uid);
            throw new AccountNotFoundException("YubiKey id is not recognized in registry");
        }

        try {
            val response = this.client.verify(otp);
            val status = response.getStatus();
            if (status.compareTo(ResponseStatus.OK) == 0) {
                LOGGER.debug("YubiKey response status [{}] at [{}]", status, response.getTimestamp());
                return createHandlerResult(yubiKeyCredential, this.principalFactory.createPrincipal(uid));
            }
            throw new FailedLoginException("Authentication failed with status: " + status);
        } catch (final YubicoVerificationException | YubicoValidationFailure e) {
            LOGGER.error(e.getMessage(), e);
            throw new FailedLoginException("YubiKey validation failed: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return YubiKeyCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        return YubiKeyCredential.class.isAssignableFrom(credential.getClass());
    }
}
