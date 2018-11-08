package org.apereo.cas.configuration.model.support.mongo;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link MongoAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-mongo")
@Getter
@Setter
public class MongoAuthenticationProperties extends SingleCollectionMongoDbProperties {

    private static final long serialVersionUID = -7304734732383722585L;

    /**
     * Attributes to fetch from Mongo.
     */
    private String attributes;

    /**
     * Attributes that holds the username.
     */
    private String usernameAttribute = "username";

    /**
     * Attribute that holds the password.
     */
    private String passwordAttribute = "password";

    /**
     * Attribute that would be used to establish the authenticated profile.
     */
    private String principalIdAttribute;

    /**
     * Password encoder settings for the authentication handler.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * This is principal transformation properties.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Name of the authentication handler.
     */
    private String name;

    public MongoAuthenticationProperties() {
        setCollection("users");
    }
}
