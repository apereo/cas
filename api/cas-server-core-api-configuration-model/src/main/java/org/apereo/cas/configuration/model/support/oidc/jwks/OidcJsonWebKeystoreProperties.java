package org.apereo.cas.configuration.model.support.oidc.jwks;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link OidcJsonWebKeystoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OidcJsonWebKeystoreProperties")
public class OidcJsonWebKeystoreProperties implements Serializable {
    private static final long serialVersionUID = -1696060572027445151L;

    /**
     * Core JWKS settings and properties.
     */
    @NestedConfigurationProperty
    private OidcJsonWebKeystoreCoreProperties core = new OidcJsonWebKeystoreCoreProperties();

    /**
     * Fetch JWKS via the file system.
     */
    @NestedConfigurationProperty
    private FileSystemOidcJsonWebKeystoreProperties fileSystem = new FileSystemOidcJsonWebKeystoreProperties();

    /**
     * Fetch JWKS via a REST endpoint.
     */
    @NestedConfigurationProperty
    private RestfulOidcJsonWebKeystoreProperties rest = new RestfulOidcJsonWebKeystoreProperties();

    /**
     * Fetch JWKS via a Groovy script.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties groovy = new SpringResourceProperties();

    /**
     * Fetch JWKS via MongoDb instances.
     */
    @NestedConfigurationProperty
    private MongoDbOidcJsonWebKeystoreProperties mongo = new MongoDbOidcJsonWebKeystoreProperties();

    /**
     * Fetch JWKS via a relational database and JPA.
     */
    @NestedConfigurationProperty
    private JpaOidcJsonWebKeystoreProperties jpa = new JpaOidcJsonWebKeystoreProperties();

    /**
     * OIDC key rotation properties.
     */
    @NestedConfigurationProperty
    private OidcJsonWebKeyStoreRotationProperties rotation = new OidcJsonWebKeyStoreRotationProperties();

    /**
     * OIDC key revocation properties.
     */
    @NestedConfigurationProperty
    private OidcJsonWebKeyStoreRevocationProperties revocation = new OidcJsonWebKeyStoreRevocationProperties();
}
