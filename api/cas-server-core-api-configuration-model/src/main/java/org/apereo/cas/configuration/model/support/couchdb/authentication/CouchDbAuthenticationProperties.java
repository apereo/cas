package org.apereo.cas.configuration.model.support.couchdb.authentication;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link CouchDbAuthenticationProperties}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-couchdb-authentication")
@Accessors(chain = true)
public class CouchDbAuthenticationProperties extends BaseCouchDbProperties {

    private static final long serialVersionUID = 1830797033934229732L;
    /**
     * Principal transformation settings.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Attributes to fetch from CouchDb.
     */
    private String attributes;

    /**
     * The name of the authentication handler.
     */
    private String name;

    /**
     * Password encoder settings for this handler.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Order of authentication handler in chain.
     */
    private int order = Integer.MAX_VALUE;

    /**
     * Username attribute to fetch and compare against credential.
     */
    @RequiredProperty
    private String usernameAttribute = "username";

    /**
     * Password attribute to fetch and compare against credential.
     */
    @RequiredProperty
    private String passwordAttribute = "password";

    public CouchDbAuthenticationProperties() {
        setDbName("users");
    }
}
