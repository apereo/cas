package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link JdbcSecurityActuatorEndpointsMonitorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-monitor", automated = true)
@JsonFilter("JdbcSecurityActuatorEndpointsMonitorProperties")
@Accessors(chain = true)
public class JdbcSecurityActuatorEndpointsMonitorProperties extends AbstractJpaProperties {

    private static final long serialVersionUID = 2625666117528467867L;

    /**
     * Prefix to add to the role.
     */
    private String rolePrefix;

    /**
     * Query to execute in order to authenticate users via JDBC.
     * Example:
     * {@code SELECT username,password,enabled FROM users WHERE username=?}
     */
    private String query;

    /**
     * Password encoder properties.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();
}
