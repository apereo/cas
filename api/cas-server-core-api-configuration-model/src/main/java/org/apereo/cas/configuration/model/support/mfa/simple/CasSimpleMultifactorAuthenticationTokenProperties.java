package org.apereo.cas.configuration.model.support.mfa.simple;

import module java.base;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTokenProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-simple-mfa")
@Getter
@Setter
@Accessors(chain = true)
public class CasSimpleMultifactorAuthenticationTokenProperties extends BaseMultifactorAuthenticationProviderProperties {
    @Serial
    private static final long serialVersionUID = -6333748853833491119L;

    /**
     * Settings for token management when
     * tokens are managed by CAS itself.
     */
    @NestedConfigurationProperty
    private CoreCasSimpleMultifactorAuthenticationTokenProperties core =
        new CoreCasSimpleMultifactorAuthenticationTokenProperties();

    /**
     * Settings for token management when
     * tokens are managed by a REST endpoint/API.
     */
    @NestedConfigurationProperty
    private RestfulCasSimpleMultifactorAuthenticationTokenProperties rest =
        new RestfulCasSimpleMultifactorAuthenticationTokenProperties();
}
