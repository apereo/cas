package org.apereo.cas.adaptors.yubikey;

import com.yubico.client.v2.ResponseStatus;
import com.yubico.client.v2.VerificationResponse;
import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.exceptions.YubicoValidationFailure;
import com.yubico.client.v2.exceptions.YubicoVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.yubikey.registry.OpenYubiKeyAccountRegistry;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

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
public class YubiKeyAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {



    private final YubiKeyAccountRegistry registry;
    private final YubicoClient client;

    /**
     * Prepares the Yubico client with the received clientId and secretKey. If you wish to
     * limit the usage of this handler only to a particular set of yubikey accounts for a special
     * group of users, you may verify an compliant implementation of {@link YubiKeyAccountRegistry}.
     * By default, all accounts are allowed.
     *
     * @param name             the name
     * @param servicesManager  the services manager
     * @param principalFactory the principal factory
     * @param client           the client
     * @param registry         the account registry which holds registrations.
     */
    public YubiKeyAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                        final PrincipalFactory principalFactory,
                                        final YubicoClient client,
                                        final YubiKeyAccountRegistry registry) {
        super(name, servicesManager, principalFactory, null);
        this.registry = registry;
        this.client = client;
    }

    public YubiKeyAuthenticationHandler(final YubicoClient client) {
        this(StringUtils.EMPTY, null, null,
            client, new OpenYubiKeyAccountRegistry());
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        final YubiKeyCredential yubiKeyCredential = (YubiKeyCredential) credential;

        final String otp = yubiKeyCredential.getToken();

        if (!YubicoClient.isValidOTPFormat(otp)) {
            LOGGER.debug("Invalid OTP format [{}]", otp);
            throw new AccountNotFoundException("OTP format is invalid");
        }

        final Authentication authentication = WebUtils.getInProgressAuthentication();
        if (authentication == null) {
            throw new IllegalArgumentException("CAS has no reference to an authentication event to locate a principal");
        }
        final Principal principal = authentication.getPrincipal();
        final String uid = principal.getId();
        final String publicId = YubicoClient.getPublicId(otp);
        if (!this.registry.isYubiKeyRegisteredFor(uid, publicId)) {
            LOGGER.debug("YubiKey public id [{}] is not registered for user [{}]", publicId, uid);
            throw new AccountNotFoundException("YubiKey id is not recognized in registry");
        }

        try {
            final VerificationResponse response = this.client.verify(otp);
            final ResponseStatus status = response.getStatus();
            if (status.compareTo(ResponseStatus.OK) == 0) {
                LOGGER.debug("YubiKey response status [{}] at [{}]", status, response.getTimestamp());
                return createHandlerResult(yubiKeyCredential, this.principalFactory.createPrincipal(uid), null);
            }
            throw new FailedLoginException("Authentication failed with status: " + status);
        } catch (final YubicoVerificationException | YubicoValidationFailure e) {
            LOGGER.error(e.getMessage(), e);
            throw new FailedLoginException("YubiKey validation failed: " + e.getMessage());
        }
    }

    public YubiKeyAccountRegistry getRegistry() {
        return this.registry;
    }

    public YubicoClient getClient() {
        return this.client;
    }

    @Override
    public boolean supports(final Credential credential) {
        return YubiKeyCredential.class.isAssignableFrom(credential.getClass());
    }
}
