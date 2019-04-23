package org.apereo.cas.web.security;

import org.apereo.cas.config.CasWebAppSecurityConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * This is {@link CasWebSecurityConfigurerAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CasWebAppSecurityConfiguration.class,
    SecurityAutoConfiguration.class,
    EndpointAutoConfiguration.class,
    WebEndpointAutoConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
})
@WebAppConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
@TestPropertySource(properties = {
    "cas.monitor.endpoints.jaas.login-config=classpath:/jaas-endpoints.conf",
    "cas.monitor.endpoints.jaas.login-context-name=CAS",

    "cas.monitor.endpoints.ldap.ldapUrl=ldap://localhost:10389",
    "cas.monitor.endpoints.ldap.useSsl=false",
    "cas.monitor.endpoints.ldap.baseDn=ou=people,dc=example,dc=org",
    "cas.monitor.endpoints.ldap.searchFilter=uid={user}",
    "cas.monitor.endpoints.ldap.bindDn=cn=Directory Manager",
    "cas.monitor.endpoints.ldap.bindCredential=password",

    "cas.monitor.endpoints.jdbc.query=SELECT * FROM USERS",
    "cas.monitor.endpoints.jdbc.role-prefix=USER_"
})
public class CasWebSecurityConfigurerAdapterTests {
    @Test
    public void verifyOperation() {
    }
}
