package org.apereo.cas.configuration.model.support.generic;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link JsonResourceAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-generic")
@Getter
@Setter
@Accessors(chain = true)
public class JsonResourceAuthenticationProperties extends SpringResourceProperties {

    private static final long serialVersionUID = 1079027841236526083L;

    /**
     * Password policy settings.
     */
    @NestedConfigurationProperty
    private PasswordPolicyProperties passwordPolicy = new PasswordPolicyProperties();

    /**
     * Password encoder properties.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Principal transformation settings for this authentication.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Authentication handler name used to verify credentials in the file.
     */
    private String name;
}
