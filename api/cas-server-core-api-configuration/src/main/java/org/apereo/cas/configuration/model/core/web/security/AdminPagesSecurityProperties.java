package org.apereo.cas.configuration.model.core.web.security;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link AdminPagesSecurityProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Slf4j
@Getter
@Setter
public class AdminPagesSecurityProperties implements Serializable {

    private static final long serialVersionUID = 9129787932447507179L;

    /**
     * The IP address pattern that can control access to the admin status endpoints.
     */
    private String ip = "a^";

    /**
     * Alternative header name to use when extracting the IP address.
     * If left blank, the request's remote ip address will be pulled.
     * When dealing with proxies or load balancers, this value should likely
     * be set to {@code X-Forwarded-For}.
     */
    private String alternateIpHeaderName;

    /**
     * Roles that are required for access to the admin status endpoint
     * in the event that access is controlled via external authentication
     * means such as Spring Security's authentication providers.
     */
    private List<String> adminRoles = Stream.of("ROLE_ADMIN", "ROLE_ACTUATOR").collect(Collectors.toList());

    /**
     * CAS server login URL to use. 
     * When defined, will begin to protect the access status endpoints via CAS itself.
     */
    private String loginUrl;

    /**
     * The service parameter for the admin status endpoint. 
     * This is typically set to the dashboard url as the initial starting point
     * for the redirect.
     */
    private String service;

    /**
     * List of users allowed access to the admin status endpoint
     * provided CAS is controlling access to the status endpoint.
     * If you decide to protect other administrative endpoints via CAS itself, 
     * you will need to provide a reference to the list of authorized users in the CAS configuration. 
     */
    private Resource users;

    /**
     * Whether Spring Boot's actuator endpoints should show up on the dashboard.
     */
    private boolean actuatorEndpointsEnabled;

    /**
     * Enable Spring Security's JDBC authentication provider
     * for admin status authorization and access control.
     */
    private Jdbc jdbc = new Jdbc();

    /**
     * Enable Spring Security's LDAP authentication provider
     * for admin status authorization and access control.
     */
    private Ldap ldap = new Ldap();

    /**
     * Enable Spring Security's JAAS authentication provider
     * for admin status authorization and access control.
     */
    private Jaas jaas = new Jaas();

    @Getter
    @Setter
    public static class Jaas implements Serializable {

        private static final long serialVersionUID = -3024678577827371641L;

        /**
         * JAAS login resource file.
         */
        private Resource loginConfig;

        /**
         * If set, a call to {@code Configuration#refresh()} 
         * will be made by {@code #configureJaas(Resource)} method.
         */
        private boolean refreshConfigurationOnStartup = true;

        /**
         * The login context name should coincide with a given index in the login config specified.
         * This name is used as the index to the configuration specified in the login config property.
         * 
<pre>
JAASTest {
    org.springframework.security.authentication.jaas.TestLoginModule required;
};
</pre>
         In the above example, {@code JAASTest} should be set as the context name.
         */
        private String loginContextName;
    }

    @Getter
    @Setter
    public static class Ldap extends AbstractLdapAuthenticationProperties {

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
    public static class Jdbc extends AbstractJpaProperties {

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
}
