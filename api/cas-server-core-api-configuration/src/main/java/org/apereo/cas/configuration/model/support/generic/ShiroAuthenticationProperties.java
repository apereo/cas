package org.apereo.cas.configuration.model.support.generic;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Configuration properties class for shiro.authn.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-shiro")
@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class ShiroAuthenticationProperties extends SpringResourceProperties {

    private static final long serialVersionUID = 8997401036330472417L;

    /**
     * Required roles that should be authorized by Shiro.
     */
    private Set<String> requiredRoles = new HashSet<>();

    /**
     * Required permissions that should be authorized by Shiro.
     */
    private Set<String> requiredPermissions = new HashSet<>();

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
