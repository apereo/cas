package org.apereo.cas.authorization;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link EndpointLdapAuthenticationProviderGroupsBasedTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnabledIfListeningOnPort(port = 10389)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = {
    "cas.monitor.endpoints.ldap.ldap-authz.group-filter=businessCategory={user}",
    "cas.monitor.endpoints.ldap.ldap-authz.group-base-dn=ou=people,dc=example,dc=org",
    "cas.monitor.endpoints.ldap.ldap-authz.base-dn=ou=people,dc=example,dc=org",
    "cas.monitor.endpoints.ldap.ldap-authz.search-filter=cn={user}",
    "cas.monitor.endpoints.ldap.ldap-authz.group-attribute=roomNumber",
    "cas.monitor.endpoints.ldap.ldap-authz.group-prefix=ROLE_",
    "cas.monitor.endpoints.ldap.ldap-url=ldap://localhost:10389",
    "cas.monitor.endpoints.ldap.base-dn=ou=people,dc=example,dc=org",
    "cas.monitor.endpoints.ldap.search-filter=cn={user}",
    "cas.monitor.endpoints.ldap.bind-dn=cn=Directory Manager",
    "cas.monitor.endpoints.ldap.bind-credential=password"
})
@Tag("LdapAuthentication")
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class EndpointLdapAuthenticationProviderGroupsBasedTests extends BaseEndpointLdapAuthenticationProviderTests {

    @Test
    void verifyAuthorizedByGroup() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(List.of("ROLE_888"));
        val ldap = casProperties.getMonitor().getEndpoints().getLdap();
        val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
        val authenticator = LdapUtils.newLdaptiveAuthenticator(ldap);
        val provider = new EndpointLdapAuthenticationProvider(ldap, securityProperties, connectionFactory, authenticator);
        assertNotNull(provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "123456")));
        assertAll(provider::destroy);
    }
}
