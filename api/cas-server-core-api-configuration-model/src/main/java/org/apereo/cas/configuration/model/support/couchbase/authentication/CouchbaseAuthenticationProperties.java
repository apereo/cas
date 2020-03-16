package org.apereo.cas.configuration.model.support.couchbase.authentication;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.couchbase.BaseCouchbaseProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link CouchbaseAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-couchbase-authentication")
public class CouchbaseAuthenticationProperties extends BaseCouchbaseProperties {

    private static final long serialVersionUID = -7257332242368463818L;

    /**
     * Principal transformation settings.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

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
    private String passwordAttribute = "psw";
}
