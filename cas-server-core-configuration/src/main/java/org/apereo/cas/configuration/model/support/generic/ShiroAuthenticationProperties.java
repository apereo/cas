package org.apereo.cas.configuration.model.support.generic;

import org.apereo.cas.configuration.support.AbstractConfigProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration properties class for shiro.authn.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "shiro.authn", ignoreUnknownFields = false)
public class ShiroAuthenticationProperties extends AbstractConfigProperties {

    private Set<String> requiredRoles = new HashSet<>();

    private Set<String> requiredPermissions = new HashSet<>();

    public ShiroAuthenticationProperties() {
        super.getConfig().setLocation(new ClassPathResource("shiro.ini"));
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
