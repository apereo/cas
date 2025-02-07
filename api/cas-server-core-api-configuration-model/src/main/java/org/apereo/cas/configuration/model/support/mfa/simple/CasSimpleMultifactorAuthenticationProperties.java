package org.apereo.cas.configuration.model.support.mfa.simple;

import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.model.support.phone.PhoneProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;

/**
 * This is {@link CasSimpleMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-simple-mfa")
@Getter
@Setter
@Accessors(chain = true)

public class CasSimpleMultifactorAuthenticationProperties extends BaseMultifactorAuthenticationProviderProperties {
    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-simple";

    @Serial
    private static final long serialVersionUID = -9211748853833491119L;

    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

    /**
     * Properties related to token management and policy.
     */
    @NestedConfigurationProperty
    private CasSimpleMultifactorAuthenticationTokenProperties token =
        new CasSimpleMultifactorAuthenticationTokenProperties();

    /**
     * Email settings for notifications.
     */
    @NestedConfigurationProperty
    private CasSimpleMultifactorAuthenticationEmailProperties mail = new CasSimpleMultifactorAuthenticationEmailProperties();

    /**
     * SMS settings for notifications.
     */
    @NestedConfigurationProperty
    private SmsProperties sms = new SmsProperties();

    /**
     * Phone call settings for notifications.
     */
    @NestedConfigurationProperty
    private PhoneProperties phone = new PhoneProperties();

    /**
     * Settings related to throttling requests using bucket4j.
     */
    @NestedConfigurationProperty
    private CasSimpleMultifactorAuthenticationBucket4jProperties bucket4j =
        new CasSimpleMultifactorAuthenticationBucket4jProperties();

    public CasSimpleMultifactorAuthenticationProperties() {
        setId(DEFAULT_IDENTIFIER);
    }
}
