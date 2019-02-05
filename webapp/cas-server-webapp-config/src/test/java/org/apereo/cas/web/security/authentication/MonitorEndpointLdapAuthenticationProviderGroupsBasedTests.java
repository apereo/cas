package org.apereo.cas.web.security.authentication;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MonitorEndpointLdapAuthenticationProviderGroupsBasedTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.monitor.endpoints.ldap.ldapAuthz.groupFilter=businessCategory={user}",
    "cas.monitor.endpoints.ldap.ldapAuthz.groupBaseDn=ou=people,dc=example,dc=org",
    "cas.monitor.endpoints.ldap.ldapAuthz.baseDn=ou=people,dc=example,dc=org",
    "cas.monitor.endpoints.ldap.ldapAuthz.searchFilter=cn={user}",
    "cas.monitor.endpoints.ldap.ldapAuthz.groupAttribute=roomNumber",
    "cas.monitor.endpoints.ldap.ldapAuthz.groupPrefix=ROLE_"
})
@EnabledIfContinuousIntegration
public class MonitorEndpointLdapAuthenticationProviderGroupsBasedTests extends BaseMonitorEndpointLdapAuthenticationProviderTests {

    @Test
    public void verifyAuthorizedByGroup() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(Collections.singletonList("ROLE_888"));
        val provider = new MonitorEndpointLdapAuthenticationProvider(casProperties.getMonitor().getEndpoints().getLdap(), securityProperties);
        val token = provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "123456"));
        assertNotNull(token);
    }
}
