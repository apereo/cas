package org.apereo.cas.configuration.model.support.mfa.duo;

import org.apereo.cas.configuration.model.core.web.session.SessionStorageTypes;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;

/**
 * This is {@link DuoSecurityMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-duo")
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(of = {"duoApiHost", "duoIntegrationKey", "duoSecretKey"}, callSuper = true)

public class DuoSecurityMultifactorAuthenticationProperties extends BaseMultifactorAuthenticationProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-duo";

    @Serial
    private static final long serialVersionUID = -4655375354167880807L;

    /**
     * Duo integration key.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String duoIntegrationKey;

    /**
     * Duo secret key.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String duoSecretKey;

    /**
     * Duo API host and url.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String duoApiHost;

    /**
     * Settings for Duo registration of unenrolled accounts.
     */
    @NestedConfigurationProperty
    private DuoSecurityMultifactorAuthenticationRegistrationProperties registration =
        new DuoSecurityMultifactorAuthenticationRegistrationProperties();

    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

    /**
     * When set to {@code true}, CAS will contact Duo Security
     * to check for user's account status and to evaluate whether
     * user qualifies for multifactor authentication from Duo's perspective.
     * When disabled, user account status is set to authenticate with Duo
     * and the API call will never be made.
     * Account status checking requires a particular Duo Security integration type
     * that allows CAS to make API calls to Duo Security with enough permissions
     * to get back user account details. Wrong integration types will result in
     * API errors and warnings in the logs, forcing CAS to ignore the user account status
     * and move on with the authentication attempt and flow.
     */
    private boolean accountStatusEnabled = true;

    /**
     * When enabled, this option allows CAS to use Duo Security
     * as a CAS-owned passwordless authentication provider and account store. Note that
     * this has nothing to do with Duo Security's "Passwordless/PassKey" capabilities,
     * or PassKey/WebAuthn capabilities of CAS that act as a separate multifactor authentication provider.
     * This solely controls the passwordless authentication feature that is provided by CAS directly.
     * <p>
     * When enabled, CAS will contact Duo Security to look up eligible passwordless accounts.
     * If the account is registered with Duo Security, CAS will switch to
     * a passwordless flow and will use the user's registered device to send a push notification.
     * User's registered with Duo Security must have a valid email address and a mobile/phone device.
     * <p>
     * This functionality requires that CAS is already equipped with Passwordless authentication.
     */
    private boolean passwordlessAuthenticationEnabled;

    /**
     * Duo admin integration key.
     */
    @ExpressionLanguageCapable
    private String duoAdminIntegrationKey;

    /**
     * Duo admin secret key.
     */
    @ExpressionLanguageCapable
    private String duoAdminSecretKey;

    /**
     * When set to true, authentication metadata and profile attributes (if any) are collected
     * from Duo Security and collected as CAS attributes.
     */
    private boolean collectDuoAttributes = true;

    /**
     * The principal attribute that would be used to resolve
     * the username sent to Duo Security, and one that would
     * also be used to verify the Duo Security response and exchange.
     * If undefined or not found, the default principal id would be used.
     */
    private String principalAttribute;

    /**
     * Indicates whether session data,
     * collected as part of Duo flows and requests
     * that are kept by the local storage, or should be replicated
     * across the cluster using the ticket registry.
     * Note that {@link SessionStorageTypes#HTTP} is not applicable here.
     */
    private SessionStorageTypes sessionStorageType = SessionStorageTypes.BROWSER_STORAGE;
    
    public DuoSecurityMultifactorAuthenticationProperties() {
        setId(DEFAULT_IDENTIFIER);
    }
}
