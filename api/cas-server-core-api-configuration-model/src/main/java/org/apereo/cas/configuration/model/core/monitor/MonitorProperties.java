package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.apereo.cas.configuration.model.support.memcached.BaseMemcachedProperties;
import org.apereo.cas.configuration.model.support.mongo.BaseMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuration properties class for cas.monitor.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-monitor", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class MonitorProperties implements Serializable {
    private static final long serialVersionUID = -7047060071480971606L;

    /**
     * The free memory threshold for the memory monitor.
     * If the amount of free memory available reaches this point
     * the memory monitor will report back a warning status as a health check.
     */
    private int freeMemThreshold = 10;

    /**
     * Options for monitoring the status a nd production of TGTs.
     */
    private Tgt tgt = new Tgt();

    /**
     * Options for monitoring the status a nd production of STs.
     */
    private St st = new St();

    /**
     * Options for monitoring the Load on a production server.
     * Load averages are "system load averages" that show the running thread
     * (task) demand on the system as an average number of running plus waiting
     * threads. This measures demand, which can be greater than what the system
     * is currently processing.
     */
    private Load load = new Load();

    /**
     * Warning options that generally deal with cache-based resources, etc.
     */
    @NestedConfigurationProperty
    private MonitorWarningProperties warn = new MonitorWarningProperties();

    /**
     * Options for monitoring JDBC resources.
     */
    private Jdbc jdbc = new Jdbc();

    /**
     * Options for monitoring LDAP resources.
     */
    private List<LdapMonitorProperties> ldap = new ArrayList<>(0);

    /**
     * Options for monitoring Memcached resources.
     */
    private Memcached memcached = new Memcached();

    /**
     * Options for monitoring MongoDb resources.
     */
    private MongoDb mongo = new MongoDb();

    /**
     * Properties relevant to endpoint security, etc.
     */
    private Endpoints endpoints = new Endpoints();

    @RequiresModule(name = "cas-server-core-monitor", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class St implements Serializable {

        private static final long serialVersionUID = -8167395674267219982L;

        /**
         * Warning settings for this monitor.
         */
        @NestedConfigurationProperty
        private MonitorWarningProperties warn = new MonitorWarningProperties(5000);
    }

    @RequiresModule(name = "cas-server-core-monitor", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Tgt implements Serializable {

        private static final long serialVersionUID = -2756454350350278724L;

        /**
         * Warning options for monitoring TGT production.
         */
        @NestedConfigurationProperty
        private MonitorWarningProperties warn = new MonitorWarningProperties(10000);
    }

    @RequiresModule(name = "cas-server-core-monitor", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Load implements Serializable {

        private static final long serialVersionUID = 5504478373010611957L;

        /**
         * Warning settings for this monitor.
         */
        @NestedConfigurationProperty
        private MonitorWarningProperties warn = new MonitorWarningProperties(25);
    }

    @RequiresModule(name = "cas-server-support-memcached-monitor")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Memcached extends BaseMemcachedProperties {

        private static final long serialVersionUID = -9139788158851782673L;
    }

    @RequiresModule(name = "cas-server-support-mongo-monitor")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class MongoDb extends BaseMongoDbProperties {

        private static final long serialVersionUID = -1918436901491275547L;
    }

    @RequiresModule(name = "cas-server-support-jdbc-monitor")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Jdbc extends AbstractJpaProperties {

        private static final long serialVersionUID = -7139788158851782673L;

        /**
         * The query to execute against the database to monitor status.
         */
        private String validationQuery = "SELECT 1";

        /**
         * When monitoring the JDBC connection pool, indicates the amount of time the operation must wait
         * before it times outs and considers the pool in bad shape.
         */
        private String maxWait = "PT5S";
    }

    @RequiresModule(name = "cas-server-support-reports", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Endpoints implements Serializable {
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
             * <pre>
             * JAASTest {
             * org.springframework.security.authentication.jaas.TestLoginModule required;
             * };
             * </pre>
             * In the above example, {@code JAASTest} should be set as the context name.
             */
            private String loginContextName;
        }

        @Getter
        @Setter
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

        public Endpoints() {
            val defaultProps = new ActuatorEndpointProperties();
            defaultProps.setAccess(Stream.of(ActuatorEndpointProperties.EndpointAccessLevel.DENY).collect(Collectors.toList()));
            getEndpoint().put("defaults", defaultProps);
        }

        public ActuatorEndpointProperties getDefaultEndpointProperties() {
            return getEndpoint().get("defaults");
        }
    }
}
