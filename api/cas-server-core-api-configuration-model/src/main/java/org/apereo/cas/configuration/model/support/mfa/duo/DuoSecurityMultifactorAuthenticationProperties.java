package org.apereo.cas.configuration.model.support.mfa.duo;

import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("DuoSecurityMultifactorProperties")
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
