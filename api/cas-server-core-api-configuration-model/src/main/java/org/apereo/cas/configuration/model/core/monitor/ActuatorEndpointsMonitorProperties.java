package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;

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
    private JaasSecurity jaas = new JaasSecurity();

    /**
     * Enable Spring Security's JDBC authentication provider
     * for admin status authorization and access control.
     */
    private JdbcSecurity jdbc = new JdbcSecurity();

    /**
     * Enable Spring Security's LDAP authentication provider
     * for admin status authorization and access control.
     */
    private LdapSecurity ldap = new LdapSecurity();

    /**
     * Control whether access to endpoints can be controlled
     * via form-based login over the web via a special admin login endpoint.
     */
    private boolean formLoginEnabled;

    @Getter
    @Setter
    @RequiresModule(name = "cas-server-core-monitor", automated = true)
    public static class JaasSecurity implements Serializable {

        private static final long serialVersionUID = -3024678577827371641L;

        /**
         * JAAS login resource file.
         */
        private transient Resource loginConfig;

        /**
         * If set, a call to {@code Configuration#refresh()}
         * will be made by {@code #configureJaas(Resource)} method.
         */
        private boolean refreshConfigurationOnStartup = true;

        /**
         * The login context name should coincide with a given index in the login config specified.
         * This name is used as the index to the configuration specified in the login config property.
         *
         * &lt;pre&gt;
         * JAASTest {
         * org.springframework.security.authentication.jaas.TestLoginModule required;
         * };
         * &lt;/pre&gt;
         * In the above example, {@code JAASTest} should be set as the context name.
         */
        private String loginContextName;
    }

    @Getter
    @Setter
    @RequiresModule(name = "cas-server-core-monitor", automated = true)
    public static class LdapSecurity extends AbstractLdapAuthenticationProperties {

        private static final long serialVersionUID = -7333244539096172557L;

        /**
         * Control authorization settings via LDAP
         * after ldap authentication.
         */
        @NestedConfigurationProperty
        private LdapAuthorizationProperties ldapAuthz = new LdapAuthorizationProperties();
    }

    @Getter
    @Setter
    @RequiresModule(name = "cas-server-core-monitor", automated = true)
    public static class JdbcSecurity extends AbstractJpaProperties {

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

    public ActuatorEndpointsMonitorProperties() {
        val defaultProps = new ActuatorEndpointProperties();
        defaultProps.setAccess(Stream.of(ActuatorEndpointProperties.EndpointAccessLevel.DENY).collect(Collectors.toList()));
        getEndpoint().put("defaults", defaultProps);
    }

    public ActuatorEndpointProperties getDefaultEndpointProperties() {
        return getEndpoint().get("defaults");
    }

}
