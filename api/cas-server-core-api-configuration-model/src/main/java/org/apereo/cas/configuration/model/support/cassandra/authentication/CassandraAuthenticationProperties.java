package org.apereo.cas.configuration.model.support.cassandra.authentication;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link CassandraAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-cassandra-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class CassandraAuthenticationProperties extends BaseCassandraProperties {

    private static final long serialVersionUID = 1369405266376125234L;

    /**
     * Name of the authentication handler.
     */
    private String name;

    /**
     * The authentication handler order in the chain.
     */
    private Integer order;

    /**
     * Username attribute to fetch and compare.
     */
    @RequiredProperty
    private String usernameAttribute;

    /**
     * Password attribute to fetch and compare.
     */
    @RequiredProperty
    private String passwordAttribute;

    /**
     * Table name to fetch credentials.
     */
    @RequiredProperty
    private String tableName;

    /**
     * The authentication query to use when searching for users.
     */
    private String query = "SELECT * FROM %s WHERE %s = ? ALLOW FILTERING";

    /**
     * Password encoding settings for this authentication.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Principal transformation settings for this authentication.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();
}
