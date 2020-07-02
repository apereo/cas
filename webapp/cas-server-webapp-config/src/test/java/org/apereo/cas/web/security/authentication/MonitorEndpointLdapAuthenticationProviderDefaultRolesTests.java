package org.apereo.cas.web.security.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MonitorEndpointLdapAuthenticationProviderDefaultRolesTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnabledIfPortOpen(port = 10389)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Ldap")
public class MonitorEndpointLdapAuthenticationProviderDefaultRolesTests extends BaseMonitorEndpointLdapAuthenticationProviderTests {

    @Test
    public void verifyAuthorizedByRole() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(List.of("ROLE_888"));
        val ldap = casProperties.getMonitor().getEndpoints().getLdap();
        val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
        val authenticator = LdapUtils.newLdaptiveAuthenticator(ldap);
        val provider = new MonitorEndpointLdapAuthenticationProvider(ldap, securityProperties, connectionFactory, authenticator);
        assertThrows(InsufficientAuthenticationException.class,
            () -> provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", null)));
        val token = provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "123456"));
        assertNotNull(token);
    }

    @Test
    public void verifyEmptyRoles() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(List.of());
        val ldap = casProperties.getMonitor().getEndpoints().getLdap();
        val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
        val authenticator = LdapUtils.newLdaptiveAuthenticator(ldap);
        val provider = new MonitorEndpointLdapAuthenticationProvider(ldap, securityProperties, connectionFactory, authenticator);
        val token = provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "123456"));
        assertNotNull(token);
        assertTrue(token.getAuthorities().isEmpty());
    }
}
