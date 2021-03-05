package org.apereo.cas.web.security.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link EndpointLdapAuthenticationProviderRolesBasedTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.monitor.endpoints.ldap.ldapAuthz.role-attribute=roomNumber",
    "cas.monitor.endpoints.ldap.ldap-authz.search-filter=cn={user}",
    "cas.monitor.endpoints.ldap.ldap-authz.base-dn=ou=people,dc=example,dc=org",
    "cas.monitor.endpoints.ldap.ldap-authz.role-prefix=ROLE_"
})
@EnabledIfPortOpen(port = 10389)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Ldap")
public class EndpointLdapAuthenticationProviderRolesBasedTests extends BaseEndpointLdapAuthenticationProviderTests {

    @Test
    public void verifyAuthorizedByRole() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(List.of("ROLE_888"));
        val ldap = casProperties.getMonitor().getEndpoints().getLdap();
        val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
        val authenticator = LdapUtils.newLdaptiveAuthenticator(ldap);
        val provider = new EndpointLdapAuthenticationProvider(ldap, securityProperties, connectionFactory, authenticator);
        assertTrue(provider.supports(UsernamePasswordAuthenticationToken.class));
        val token = provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "123456"));
        assertNotNull(token);
        assertAll(provider::destroy);
    }

    @Test
    public void verifyUnauthorizedByRole() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(List.of("SOME_BAD_ROLE"));
        val ldap = casProperties.getMonitor().getEndpoints().getLdap();
        val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
        val authenticator = LdapUtils.newLdaptiveAuthenticator(ldap);
        val provider = new EndpointLdapAuthenticationProvider(ldap, securityProperties, connectionFactory, authenticator);
        assertThrows(BadCredentialsException.class, () -> provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "123456")));
        assertAll(provider::destroy);
    }

    @Test
    public void verifyUserNotFound() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(List.of("SOME_BAD_ROLE"));
        val ldap = casProperties.getMonitor().getEndpoints().getLdap();
        val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
        val authenticator = LdapUtils.newLdaptiveAuthenticator(ldap);
        val provider = new EndpointLdapAuthenticationProvider(ldap, securityProperties, connectionFactory, authenticator);
        assertThrows(InsufficientAuthenticationException.class,
            () -> provider.authenticate(new UsernamePasswordAuthenticationToken("UNKNOWN_USER", "123456")));
        assertAll(provider::destroy);
    }

    @Test
    public void verifyUserBadPassword() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(List.of("SOME_BAD_ROLE"));
        val ldap = casProperties.getMonitor().getEndpoints().getLdap();
        val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
        val authenticator = LdapUtils.newLdaptiveAuthenticator(ldap);
        val provider = new EndpointLdapAuthenticationProvider(ldap, securityProperties, connectionFactory, authenticator);
        assertThrows(BadCredentialsException.class,
            () -> provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "BAD_PASSWORD")));
        assertAll(provider::destroy);
    }

}
