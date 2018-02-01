package org.apereo.cas.configuration.model.support.mongo;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link MongoAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-mongo")
@Slf4j
@Getter
@Setter
public class MongoAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = -7304734732383722585L;

    /**
     * Attributes to fetch from Mongo.
     */
    private String attributes;

    /**
     * Collection that holds credentials.
     */
    private String collectionName = "users";

    /**
     * Mongo host uri where accounts are kept.
     */
    private String mongoHostUri = "mongodb://uri";

    /**
     * Attributes that holds the username.
     */
    private String usernameAttribute = "username";

    /**
     * Attribute that holds the password.
     */
    private String passwordAttribute = "password";

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
}
