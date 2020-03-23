package org.apereo.cas.configuration.model.support.generic;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration properties class for shiro.authn.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-shiro")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class ShiroAuthenticationProperties extends SpringResourceProperties {

    private static final long serialVersionUID = 8997401036330472417L;

    /**
     * Required roles that should be authorized by Shiro.
     */
    private Set<String> requiredRoles = new HashSet<>(0);

    /**
     * Required permissions that should be authorized by Shiro.
     */
    private Set<String> requiredPermissions = new HashSet<>(0);

    /**
     * Password encoder properties.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Principal transformation properties.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Name of the authentication handler.
     */
    private String name;
}
