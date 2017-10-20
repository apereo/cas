package org.apereo.cas.configuration.model.support.mongo;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link MongoAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-mongo")
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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public PrincipalTransformationProperties getPrincipalTransformation() {
        return principalTransformation;
    }

    public void setPrincipalTransformation(final PrincipalTransformationProperties principalTransformation) {
        this.principalTransformation = principalTransformation;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(final String attributes) {
        this.attributes = attributes;
    }


    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(final String collectionName) {
        this.collectionName = collectionName;
    }

    public String getMongoHostUri() {
        return mongoHostUri;
    }

    public void setMongoHostUri(final String mongoHostUri) {
        this.mongoHostUri = mongoHostUri;
    }

    public String getUsernameAttribute() {
        return usernameAttribute;
    }

    public void setUsernameAttribute(final String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }

    public String getPasswordAttribute() {
        return passwordAttribute;
    }

    public void setPasswordAttribute(final String passwordAttribute) {
        this.passwordAttribute = passwordAttribute;
    }

    public PasswordEncoderProperties getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(final PasswordEncoderProperties passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

}
