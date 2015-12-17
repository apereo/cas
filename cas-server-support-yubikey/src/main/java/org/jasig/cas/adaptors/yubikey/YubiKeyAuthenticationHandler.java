package org.jasig.cas.adaptors.yubikey;

import com.yubico.client.v2.ResponseStatus;
import com.yubico.client.v2.VerificationResponse;
import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.exceptions.YubicoValidationFailure;
import com.yubico.client.v2.exceptions.YubicoVerificationException;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;

/**
 * An authentication handler that uses the Yubico cloud validation
 * platform to authenticate one-time password tokens that are
 * issued by a YubiKey device. To use YubiCloud you need a
 * client id and an API key which must be obtained from Yubico.
 *
 * <p>For more info, please visit
 * <a href="http://yubico.github.io/yubico-java-client/">this link</a></p>
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Component("yubiKeyAuthenticationHandler")
public class YubiKeyAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler
        implements InitializingBean {

    private YubiKeyAccountRegistry registry;

    private final YubicoClient client;

    /**
     * Prepares the Yubico client with the received clientId and secretKey. If you wish to
     * limit the usage of this handler only to a particular set of yubikey accounts for a special
     * group of users, you may provide an compliant implementation of {@link YubiKeyAccountRegistry}.
     * By default, all accounts are allowed.
     *
     * @param clientId the client id
     * @param secretKey the secret key
     */
    @Autowired
    public YubiKeyAuthenticationHandler(@NotNull @Value("${yubikey.client.id:}") final Integer clientId,
                                        @NotNull @Value("${yubikey.secret.key:}") final String secretKey) {
        this.client = YubicoClient.getClient(clientId, secretKey);
    }

    @Override
    @PostConstruct
    public void afterPropertiesSet() {
        if (this.registry == null) {
            logger.warn("No YubiKey account registry is defined. All credentials are considered "
                    + "eligible for YubiKey authentication. Consider providing an account registry via [{}]",
                    YubiKeyAccountRegistry.class.getName());
        }
    }

    /**
     * {@inheritDoc}
     * Attempts to authenticate the received credentials using the Yubico cloud validation platform.
     * In this implementation, the {@link UsernamePasswordCredential#getUsername()}
     * is mapped to the {@code uid} which will be used by the plugged-in instance of the
     * {@link YubiKeyAccountRegistry}
     * and the {@link UsernamePasswordCredential#getPassword()} is the received
     * one-time password token issued by the YubiKey device.
     */
    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential)
            throws GeneralSecurityException, PreventedException {

        final String uid = transformedCredential.getUsername();
        final String otp = transformedCredential.getPassword();

        if (!YubicoClient.isValidOTPFormat(otp)) {
            logger.debug("Invalid OTP format [{}]", otp);
            throw new FailedLoginException("OTP format is invalid");
        }

        final String publicId = YubicoClient.getPublicId(otp);
        if (this.registry != null
              &&!this.registry.isYubiKeyRegisteredFor(uid, publicId)) {
            logger.debug("YubiKey public id [{}] is not registered for user [{}]", publicId, uid);
            throw new AccountNotFoundException("YubiKey id is not recognized in registry");
        }

        try {
            final VerificationResponse response = this.client.verify(otp);
            final ResponseStatus status = response.getStatus();
            if (status.compareTo(ResponseStatus.OK) == 0) {
                logger.debug("YubiKey response status {} at {}", status, response.getTimestamp());
                return createHandlerResult(transformedCredential,
                        this.principalFactory.createPrincipal(uid), null);
            }
            throw new FailedLoginException("Authentication failed with status: " + status);
        } catch (final YubicoVerificationException | YubicoValidationFailure e) {
            logger.error(e.getMessage(), e);
            throw new FailedLoginException("YubiKey validation failed: " + e.getMessage());
        }
    }


    @Autowired(required=false)
    public void setRegistry(@Qualifier("yubiKeyAccountRegistry")
                                final YubiKeyAccountRegistry registry) {
        this.registry = registry;
    }

    public YubiKeyAccountRegistry getRegistry() {
        return registry;
    }
}
