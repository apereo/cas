package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.model.support.oidc.jwks.OidcJsonWebKeystoreProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
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
public class OidcProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 813028615694269276L;

    /**
     * Control OIDC response and response mode settings.
     */
    @NestedConfigurationProperty
    private OidcResponseProperties response = new OidcResponseProperties();

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
     * OIDC core protocol settings.
     */
    @NestedConfigurationProperty
    private OidcIdTokenProperties idToken = new OidcIdTokenProperties();

    /**
     * OIDC CIBA protocol settings.
     */
    @NestedConfigurationProperty
    private OidcCibaProperties ciba = new OidcCibaProperties();

    /**
     * OIDC webfinger protocol settings.
     */
    @NestedConfigurationProperty
    private OidcWebFingerProperties webfinger = new OidcWebFingerProperties();

    /**
     * OIDC services settings.
     */
    @NestedConfigurationProperty
    private OidcServicesProperties services = new OidcServicesProperties();

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

    /**
     * OIDC pushed authorization requests configuration.
     */
    @NestedConfigurationProperty
    private OidcPushedAuthorizationProperties par = new OidcPushedAuthorizationProperties();

    /**
     * OIDC pushed authorization requests configuration.
     */
    @NestedConfigurationProperty
    private OidcJwtAuthorizationResponseModeProperties jarm = new OidcJwtAuthorizationResponseModeProperties();

    /**
     * OIDC handling of dynamic client registration requests and settings.
     */
    @NestedConfigurationProperty
    private OidcClientRegistrationProperties registration = new OidcClientRegistrationProperties();

    /**
     * OIDC ID assurance settings.
     */
    @NestedConfigurationProperty
    private OidcIdentityAssuranceProperties identityAssurance = new OidcIdentityAssuranceProperties();

    /**
     * Configuration properties to manage OIDC federation settings.
     */
    @NestedConfigurationProperty
    private OidcFederationProperties federation = new OidcFederationProperties();
}
