package org.apereo.cas.web.security.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MonitorEndpointLdapAuthenticationProviderGroupsBasedTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnabledIfPortOpen(port = 10389)
@SpringBootTest(classes = RefreshAutoConfiguration.class,
    properties = {
        "cas.monitor.endpoints.ldap.ldapAuthz.groupFilter=businessCategory={user}",
        "cas.monitor.endpoints.ldap.ldapAuthz.groupBaseDn=ou=people,dc=example,dc=org",
        "cas.monitor.endpoints.ldap.ldapAuthz.baseDn=ou=people,dc=example,dc=org",
        "cas.monitor.endpoints.ldap.ldapAuthz.searchFilter=cn={user}",
        "cas.monitor.endpoints.ldap.ldapAuthz.groupAttribute=roomNumber",
        "cas.monitor.endpoints.ldap.ldapAuthz.groupPrefix=ROLE_",
        "cas.monitor.endpoints.ldap.ldap-url=ldap://localhost:10389",
        "cas.monitor.endpoints.ldap.baseDn=ou=people,dc=example,dc=org",
        "cas.monitor.endpoints.ldap.searchFilter=cn={user}",
        "cas.monitor.endpoints.ldap.bindDn=cn=Directory Manager",
        "cas.monitor.endpoints.ldap.bindCredential=password"
    })
@Tag("Ldap")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MonitorEndpointLdapAuthenticationProviderGroupsBasedTests extends BaseMonitorEndpointLdapAuthenticationProviderTests {

    @Test
    public void verifyAuthorizedByGroup() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(List.of("ROLE_888"));
        val ldap = casProperties.getMonitor().getEndpoints().getLdap();
        val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
        val authenticator = LdapUtils.newLdaptiveAuthenticator(ldap);
        val provider = new MonitorEndpointLdapAuthenticationProvider(ldap, securityProperties, connectionFactory, authenticator);
        assertNotNull(provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "123456")));
        assertAll(new Executable() {
            @Override
            public void execute() throws Exception {
                provider.destroy();
            }
        });
    }
}
