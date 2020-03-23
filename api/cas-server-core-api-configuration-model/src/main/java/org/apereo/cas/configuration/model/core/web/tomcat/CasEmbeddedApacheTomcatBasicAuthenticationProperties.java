package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@Getter
@Setter
@Accessors(chain = true)
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
}
