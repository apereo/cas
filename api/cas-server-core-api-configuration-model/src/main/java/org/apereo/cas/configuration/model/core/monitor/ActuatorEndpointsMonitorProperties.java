package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class ActuatorEndpointsMonitorProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -3375777593395683691L;

    /**
     * Options for monitoring sensitive CAS endpoints and resources.
     * Acts as a parent class for all endpoints and settings
     * and exposes shortcuts so security and capability of endpoints
     * can be globally controlled from one spot and then overridden elsewhere.
     */
    private Map<String, ActuatorEndpointProperties> endpoint = new HashMap<>();

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
     * Use a static JSON file to define users that have access
     * to the actuator endpoints, etc.
     */
    @NestedConfigurationProperty
    private JsonSecurityActuatorEndpointsMonitorProperties json = new JsonSecurityActuatorEndpointsMonitorProperties();

    /**
     * Control whether access to endpoints can be controlled
     * via form-based login over the web via a special admin login endpoint.
     */
    private boolean formLoginEnabled;

    /**
     * List of endpoint patterns that will be added to the
     * Spring Security's filter chain to be completed ignored
     * and removed from security considerations and enforcements.
     * Example: {@code /endpoint.xyz} or {@code /endpoint/**}.
     */
    private List<String> ignoredEndpoints = new ArrayList<>();
    
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
