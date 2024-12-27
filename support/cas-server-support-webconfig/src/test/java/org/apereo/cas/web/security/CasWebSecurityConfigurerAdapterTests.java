package org.apereo.cas.web.security;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.web.WebAppConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasWebSecurityConfigurerAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = BaseWebSecurityTests.SharedTestConfiguration.class,
    properties = {
        "management.endpoint.info.access=UNRESTRICTED",
        "management.endpoint.beans.access=UNRESTRICTED",
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

        "cas.monitor.endpoints.default-endpoint-properties.required-ip-addresses=127.+",
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
@Tag("LdapAuthentication")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 10389)
class CasWebSecurityConfigurerAdapterTests {

    @Autowired
    @Qualifier("casWebSecurityConfigurerAdapter")
    private SecurityFilterChain casWebSecurityConfigurerAdapter;

    @Test
    void verifyOperation() {
        assertNotNull(casWebSecurityConfigurerAdapter);
    }
}
