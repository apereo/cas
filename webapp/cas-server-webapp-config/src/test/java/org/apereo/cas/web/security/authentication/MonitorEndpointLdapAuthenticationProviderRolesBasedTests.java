package org.apereo.cas.web.security.authentication;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * This is {@link MonitorEndpointLdapAuthenticationProviderRolesBasedTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
@Category(LdapCategory.class)
@TestPropertySource(properties = {
    "cas.monitor.endpoints.ldap.ldapUrl=ldap://localhost:10389",
    "cas.monitor.endpoints.ldap.useSsl=false",
    "cas.monitor.endpoints.ldap.baseDn=ou=people,dc=example,dc=org",
    "cas.monitor.endpoints.ldap.searchFilter=cn={user}",
    "cas.monitor.endpoints.ldap.bindDn=cn=Directory Manager",
    "cas.monitor.endpoints.ldap.bindCredential=password",
    "cas.monitor.endpoints.ldap.ldapAuthz.roleAttribute=roomNumber",
    "cas.monitor.endpoints.ldap.ldapAuthz.searchFilter=cn={user}",
    "cas.monitor.endpoints.ldap.ldapAuthz.baseDn=ou=people,dc=example,dc=org",
    "cas.monitor.endpoints.ldap.ldapAuthz.rolePrefix=ROLE_"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MonitorEndpointLdapAuthenticationProviderRolesBasedTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final int LDAP_PORT = 10389;

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeClass
    @SneakyThrows
    public static void bootstrap() {
        ClientInfoHolder.setClientInfo(new ClientInfo(new MockHttpServletRequest()));
        val localhost = new LDAPConnection("localhost", LDAP_PORT, "cn=Directory Manager", "password");
        localhost.connect("localhost", LDAP_PORT);
        localhost.bind("cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ClassPathResource("ldif/ldap-authz.ldif").getInputStream(), "ou=people,dc=example,dc=org");
    }

    @Before
    public void init() {
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
    }

    @Test
    public void verifyAuthorizedByRole() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(Collections.singletonList("ROLE_888"));
        val provider = new MonitorEndpointLdapAuthenticationProvider(casProperties.getMonitor().getEndpoints().getLdap(), securityProperties);
        val token = provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "123456"));
        assertNotNull(token);
    }

    @Test
    public void verifyUnauthorizedByRole() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(Collections.singletonList("SOME_BAD_ROLE"));
        val provider = new MonitorEndpointLdapAuthenticationProvider(casProperties.getMonitor().getEndpoints().getLdap(), securityProperties);
        thrown.expect(BadCredentialsException.class);
        provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "123456"));
    }

    @Test
    public void verifyUserNotFound() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(Collections.singletonList("SOME_BAD_ROLE"));
        val provider = new MonitorEndpointLdapAuthenticationProvider(casProperties.getMonitor().getEndpoints().getLdap(), securityProperties);
        thrown.expect(BadCredentialsException.class);
        provider.authenticate(new UsernamePasswordAuthenticationToken("UNKNOWN_USER", "123456"));
    }

    @Test
    public void verifyUserBadPassword() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(Collections.singletonList("SOME_BAD_ROLE"));
        val provider = new MonitorEndpointLdapAuthenticationProvider(casProperties.getMonitor().getEndpoints().getLdap(), securityProperties);
        thrown.expect(BadCredentialsException.class);
        provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "BAD_PASSWORD"));
    }

}
