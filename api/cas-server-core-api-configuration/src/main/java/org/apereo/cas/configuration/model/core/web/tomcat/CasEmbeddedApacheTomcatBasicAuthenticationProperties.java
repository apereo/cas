package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link CasEmbeddedApacheTomcatBasicAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
public class CasEmbeddedApacheTomcatBasicAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = 1164446071136700282L;
    /**
     * Enable the SSL valve for apache tomcat.
     */
    private boolean enabled;

    /**
     * Security roles for the CAS application.
     */
    private List<String> securityRoles = Stream.of("admin").collect(Collectors.toList());

    /**
     * Add an authorization role, which is a role name that will be
     * permitted access to the resources protected by this security constraint.
     */
    private List<String> authRoles = Stream.of("admin").collect(Collectors.toList());

    /**
     * Add a URL pattern to be part of this web resource collection.
     */
    private List<String> patterns = Stream.of("/*").collect(Collectors.toList());

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getSecurityRoles() {
        return securityRoles;
    }

    public void setSecurityRoles(final List<String> securityRoles) {
        this.securityRoles = securityRoles;
    }

    public List<String> getAuthRoles() {
        return authRoles;
    }

    public void setAuthRoles(final List<String> authRoles) {
        this.authRoles = authRoles;
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(final List<String> patterns) {
        this.patterns = patterns;
    }
}
