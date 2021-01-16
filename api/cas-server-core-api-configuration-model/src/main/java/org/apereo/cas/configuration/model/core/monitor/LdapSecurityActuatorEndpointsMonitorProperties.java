package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link LdapSecurityActuatorEndpointsMonitorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-monitor", automated = true)
@JsonFilter("LdapSecurityActuatorEndpointsMonitorProperties")
@Accessors(chain = true)
public class LdapSecurityActuatorEndpointsMonitorProperties extends AbstractLdapAuthenticationProperties {

    private static final long serialVersionUID = -7333244539096172557L;

    /**
     * Control authorization settings via LDAP
     * after ldap authentication.
     */
    @NestedConfigurationProperty
    private LdapAuthorizationProperties ldapAuthz = new LdapAuthorizationProperties();
}

