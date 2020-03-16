package org.apereo.cas.configuration.model.support.generic;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link RejectAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-generic")
@Getter
@Setter
@Accessors(chain = true)
public class RejectAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = -3228601837221178711L;

    /**
     * Comma-separated list of users to reject for authentication.
     */
    private String users;

    /**
     * Password encoder properties.
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
