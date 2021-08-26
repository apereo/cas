package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link ActuatorEndpointsMonitorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-reports")
@Getter
@Setter
@ToString
@Accessors(chain = true)
@JsonFilter("ActuatorEndpointsMonitorProperties")
public class ActuatorEndpointsMonitorProperties implements Serializable {
    private static final long serialVersionUID = -3375777593395683691L;

    /**
     * Options for monitoring sensitive CAS endpoints and resources.
     * Acts as a parent class for all endpoints and settings
     * and exposes shortcuts so security and capability of endpoints
     * can be globally controlled from one spot and then overridden elsewhere.
     */
    private Map<String, ActuatorEndpointProperties> endpoint = new HashMap<>(0);

    /**
     * Enable Spring Security's JAAS authentication provider
     * for admin status authorization and access control.
     */
    @NestedConfigurationProperty
    private JaasSecurityActuatorEndpointsMonitorProperties jaas = new JaasSecurityActuatorEndpointsMonitorProperties();

    /**
     * Enable Spring Security's JDBC authentication provider
     * for admin status authorization and access control.
     */
    @NestedConfigurationProperty
    private JdbcSecurityActuatorEndpointsMonitorProperties jdbc = new JdbcSecurityActuatorEndpointsMonitorProperties();

    /**
     * Enable Spring Security's LDAP authentication provider
     * for admin status authorization and access control.
     */
    @NestedConfigurationProperty
    private LdapSecurityActuatorEndpointsMonitorProperties ldap = new LdapSecurityActuatorEndpointsMonitorProperties();

    /**
     * Control whether access to endpoints can be controlled
     * via form-based login over the web via a special admin login endpoint.
     */
    private boolean formLoginEnabled;

    public ActuatorEndpointsMonitorProperties() {
        val defaultProps = new ActuatorEndpointProperties();
        defaultProps.setAccess(Stream
            .of(ActuatorEndpointProperties.EndpointAccessLevel.DENY)
            .collect(Collectors.toList()));
        getEndpoint().put("defaults", defaultProps);
    }

    public ActuatorEndpointProperties getDefaultEndpointProperties() {
        return getEndpoint().get("defaults");
    }

}
