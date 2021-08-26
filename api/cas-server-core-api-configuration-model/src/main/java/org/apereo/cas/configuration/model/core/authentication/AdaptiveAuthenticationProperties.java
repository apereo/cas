package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link AdaptiveAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class AdaptiveAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = -1840174229142982880L;

    /**
     * Adaptive authentication policy-related settings.
     */
    @NestedConfigurationProperty
    private AdaptiveAuthenticationPolicyProperties policy = new AdaptiveAuthenticationPolicyProperties();

    /**
     * Control settings that handle and calculate risky authentication attempts.
     */
    @NestedConfigurationProperty
    private RiskBasedAuthenticationProperties risk = new RiskBasedAuthenticationProperties();

    /**
     * Control settings that handle and calculate IP intelligence, etc.
     */
    @NestedConfigurationProperty
    private AdaptiveAuthenticationIPIntelligenceProperties ipIntel = new AdaptiveAuthenticationIPIntelligenceProperties();
}
