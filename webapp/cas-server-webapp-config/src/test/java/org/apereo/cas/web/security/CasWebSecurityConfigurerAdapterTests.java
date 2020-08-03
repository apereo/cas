package org.apereo.cas.web.security;

import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasWebAppSecurityConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * This is {@link CasWebSecurityConfigurerAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebAppSecurityConfiguration.class,
    SecurityAutoConfiguration.class,
    EndpointAutoConfiguration.class,
    InfoEndpointAutoConfiguration.class,
    BeansEndpointAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    WebEndpointAutoConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "management.endpoint.info.enabled=true",
        "management.endpoint.beans.enabled=true",
        "management.endpoints.web.exposure.include=*",
        
        "cas.monitor.endpoints.jaas.login-config=classpath:/jaas-endpoints.conf",
        "cas.monitor.endpoints.jaas.login-context-name=CAS",

        "cas.monitor.endpoints.ldap.ldap-url=ldap://localhost:10389",
        "cas.monitor.endpoints.ldap.base-dn=ou=people,dc=example,dc=org",
        "cas.monitor.endpoints.ldap.search-filter=uid={user}",
        "cas.monitor.endpoints.ldap.bind-dn=cn=Directory Manager",
        "cas.monitor.endpoints.ldap.bind-credential=password",

        "cas.monitor.endpoints.jdbc.query=SELECT * FROM USERS",
        "cas.monitor.endpoints.jdbc.role-prefix=USER_",

        "cas.monitor.endpoints.default-endpoint-properties.requiredIpAddresses=127.+",
        "cas.monitor.endpoints.default-endpoint-properties.access=IP_ADDRESS",

        "cas.monitor.endpoints.endpoint.health.access=IP_ADDRESS",
        "cas.monitor.endpoints.endpoint.health.required-ip-addresses=196.+",

        "cas.monitor.endpoints.endpoint.status.access=AUTHENTICATED",

        "cas.monitor.endpoints.endpoint.env.access=PERMIT",

        "cas.monitor.endpoints.endpoint.springWebflow.access=ANONYMOUS",

        "cas.monitor.endpoints.endpoint.sso.access=AUTHORITY",
        "cas.monitor.endpoints.endpoint.sso.required-authorities=EXAMPLE",

        "cas.monitor.endpoints.endpoint.info.access=ROLE",
        "cas.monitor.endpoints.endpoint.info.required-roles=EXAMPLE"
    })
@WebAppConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Ldap")
@EnabledIfPortOpen(port = 10389)
public class CasWebSecurityConfigurerAdapterTests {
    @Test
    public void verifyOperation() {
    }
}
