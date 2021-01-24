package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
     * OIDC issuer.
     */
    @RequiredProperty
    private String issuer = "http://localhost:8080/cas/oidc";

    /**
     * Skew value used to massage the authentication issue instance.
     */
    private int skew = 5;

    /**
     * Whether dynamic registration operates in {@code OPEN} or {@code PROTECTED} mode.
     */
    private String dynamicClientRegistrationMode;

    /**
     * Mapping of user-defined scopes. Key is the new scope name
     * and value is a comma-separated list of claims mapped to the scope.
     */
    private Map<String, String> userDefinedScopes = new HashMap<>(0);

    /**
     * Map fixed claims to CAS attributes.
     * Key is the existing claim name for a scope and value is the new attribute
     * that should take its place and value.
     */
    private Map<String, String> claimsMap = new HashMap<>(0);

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
