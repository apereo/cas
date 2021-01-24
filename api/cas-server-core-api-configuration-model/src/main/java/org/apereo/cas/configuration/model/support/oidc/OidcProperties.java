package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link OidcProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OidcProperties")
public class OidcProperties implements Serializable {

    private static final long serialVersionUID = 813028615694269276L;

    /**
     * Configuration properties managing the jwks settings for OIDC.
     */
    @NestedConfigurationProperty
    private OidcJsonWebKeystoreProperties jwks = new OidcJsonWebKeystoreProperties();

    /**
     * OIDC core protocol settings.
     */
    @NestedConfigurationProperty
    private OidcCoreProperties core = new OidcCoreProperties();

    /**
     * OIDC webfinger protocol settings.
     */
    @NestedConfigurationProperty
    private OidcWebFingerProperties webfinger = new OidcWebFingerProperties();

    /**
     * OIDC logout configuration.
     */
    @NestedConfigurationProperty
    private OidcLogoutProperties logout = new OidcLogoutProperties();

    /**
     * OIDC discovery configuration.
     */
    @NestedConfigurationProperty
    private OidcDiscoveryProperties discovery = new OidcDiscoveryProperties();
}
