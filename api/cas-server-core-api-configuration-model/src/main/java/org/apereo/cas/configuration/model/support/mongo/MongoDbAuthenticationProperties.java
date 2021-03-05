package org.apereo.cas.configuration.model.support.mongo;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link MongoDbAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-mongo")
@Getter
@Setter
@Accessors(chain = true)
public class MongoDbAuthenticationProperties extends SingleCollectionMongoDbProperties {

    private static final long serialVersionUID = -7304734732383722585L;

    /**
     * Attributes to fetch from Mongo (blank by default to force the pac4j legacy behavior).
     */
    private String attributes = StringUtils.EMPTY;

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

    /**
     * Order of authentication handler in chain.
     */
    private int order = Integer.MAX_VALUE;

    public MongoDbAuthenticationProperties() {
        setCollection("users");
    }
}
