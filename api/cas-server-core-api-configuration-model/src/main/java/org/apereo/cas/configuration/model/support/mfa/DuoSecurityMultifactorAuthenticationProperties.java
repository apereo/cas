package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@EqualsAndHashCode(of = {"duoApiHost", "duoIntegrationKey", "duoSecretKey", "duoApplicationKey"}, callSuper = true)
@JsonFilter("DuoSecurityMultifactorProperties")
public class DuoSecurityMultifactorAuthenticationProperties extends BaseMultifactorAuthenticationProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-duo";

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
     * The application key is a string, at least 40 characters long,
     * that you generate and keep secret from Duo.
     * This is a required setting for the WebSDK integration types.
     * Leaving this setting as blank will activate the Universal Prompt option.
     * <p>
     * You can generate a random string in Python with:
     * &lt;pre&gt;
     * import os, hashlib
     * print hashlib.sha1(os.urandom(32)).hexdigest()
     * &lt;/pre&gt;
     *
     * @deprecated since 6.4.0
     */
    @RequiredProperty
    @Deprecated(since = "6.4.0")
    @ExpressionLanguageCapable
    private String duoApplicationKey;

    /**
     * Duo API host and url.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String duoApiHost;

    /**
     * Link to a registration app, typically developed in-house
     * in order to allow new users to sign-up for duo functionality.
     * If the user account status requires enrollment and this link
     * is specified, CAS will redirect the authentication flow
     * to this registration app. Otherwise, the default duo mechanism
     * for new-user registrations shall take over.
     */
    private String registrationUrl;

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
     */
    private boolean accountStatusEnabled = true;

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

    public DuoSecurityMultifactorAuthenticationProperties() {
        setId(DEFAULT_IDENTIFIER);
    }
}
