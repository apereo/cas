package org.apereo.cas.configuration.model.support.stormpath;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link StormpathProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class StormpathProperties {

    private String apiKey;
    private String applicationId;
    private String secretkey;

    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    public PasswordEncoderProperties getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(final PasswordEncoderProperties passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final String applicationId) {
        this.applicationId = applicationId;
    }

    public String getSecretkey() {
        return secretkey;
    }

    public void setSecretkey(final String secretkey) {
        this.secretkey = secretkey;
    }
}
