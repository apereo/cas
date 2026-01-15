package org.apereo.cas.configuration.model.support.mfa.twilio;

import module java.base;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link CasTwilioMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-support-twilio-mfa")
@Getter
@Setter
@Accessors(chain = true)
public class CasTwilioMultifactorAuthenticationProperties extends BaseMultifactorAuthenticationProviderProperties {
    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-twilio";

    @Serial
    private static final long serialVersionUID = -9123748853833491119L;

    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;
    
    /**
     * Settings related to throttling requests using bucket4j.
     */
    @NestedConfigurationProperty
    private CasTwilioMultifactorAuthenticationBucket4jProperties bucket4j =
        new CasTwilioMultifactorAuthenticationBucket4jProperties();

    /**
     * Setting related to Twilio core integration, such as account id and token, etc.
     */
    @NestedConfigurationProperty
    private CasTwilioMultifactorAuthenticationCoreProperties core =
        new CasTwilioMultifactorAuthenticationCoreProperties();

    public CasTwilioMultifactorAuthenticationProperties() {
        setId(DEFAULT_IDENTIFIER);
    }
}
