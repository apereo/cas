package org.apereo.cas.configuration.model.support.generic;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;
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

    public ShiroAuthenticationProperties() {
    }

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

    public PasswordEncoderProperties getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(final PasswordEncoderProperties passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Set<String> getRequiredRoles() {
        return requiredRoles;
    }

    public void setRequiredRoles(final Set<String> requiredRoles) {
        this.requiredRoles = requiredRoles;
    }

    public Set<String> getRequiredPermissions() {
        return requiredPermissions;
    }

    public void setRequiredPermissions(final Set<String> requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }
}
