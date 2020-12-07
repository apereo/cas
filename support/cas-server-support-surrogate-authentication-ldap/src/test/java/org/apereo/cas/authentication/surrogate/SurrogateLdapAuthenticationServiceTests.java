package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.SurrogateLdapAuthenticationConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateLdapAuthenticationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateLdapAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Ldap")
@SpringBootTest(classes = {
    SurrogateLdapAuthenticationConfiguration.class,
    BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.surrogate.ldap.ldap-url=ldap://localhost:10389",
    "cas.authn.surrogate.ldap.base-dn=ou=surrogates,dc=example,dc=org",
    "cas.authn.surrogate.ldap.bind-dn=cn=Directory Manager",
    "cas.authn.surrogate.ldap.bind-credential=password",
    "cas.authn.surrogate.ldap.search-filter=cn={user}",
    "cas.authn.surrogate.ldap.surrogate-search-filter=employeeType={surrogate}",
    "cas.authn.surrogate.ldap.member-attribute-name=mail",
    "cas.authn.surrogate.ldap.member-attribute-value-regex=\\\\w+@example.org"
})
@Getter
@EnabledIfPortOpen(port = 10389)
public class SurrogateLdapAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {

    private static final int LDAP_PORT = 10389;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private SurrogateAuthenticationService service;

    @Autowired
    @Qualifier("surrogateLdapConnectionFactory")
    private ConnectionFactory surrogateLdapConnectionFactory;

    @BeforeAll
    public static void bootstrap() throws Exception {
        @Cleanup
        val localhost = new LDAPConnection("localhost", LDAP_PORT,
            "cn=Directory Manager", "password");
        localhost.connect("localhost", LDAP_PORT);
        localhost.bind("cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateEntries(
            localhost,
            new ClassPathResource("ldif/ldap-surrogates-ou.ldif").getInputStream(),
            "dc=example,dc=org");
        LdapIntegrationTestsOperations.populateEntries(
            localhost,
            new ClassPathResource("ldif/ldap-surrogate.ldif").getInputStream(),
            "ou=surrogates,dc=example,dc=org");
    }

    @Test
    public void verifyFails() {
        val su = casProperties.getAuthn().getSurrogate();
        val factory = mock(ConnectionFactory.class);
        val ldapService = new SurrogateLdapAuthenticationService(factory, su.getLdap(), servicesManager);
        assertFalse(ldapService.canAuthenticateAs("casuser",
            CoreAuthenticationTestUtils.getPrincipal(), Optional.empty()));
        ldapService.destroy();
    }

    @Test
    public void verifyNoAttr() {
        val su = casProperties.getAuthn().getSurrogate();
        val props = new SurrogateLdapAuthenticationProperties();
        BeanUtils.copyProperties(su.getLdap(), props);
        props.setMemberAttributeName("unknown");
        val ldapService = new SurrogateLdapAuthenticationService(surrogateLdapConnectionFactory, props, servicesManager);
        assertTrue(ldapService.getEligibleAccountsForSurrogateToProxy("casuser").isEmpty());
        ldapService.destroy();
    }

    @Test
    public void verifyFailsProxying() {
        val su = casProperties.getAuthn().getSurrogate();
        val factory = mock(ConnectionFactory.class);
        val props = new SurrogateLdapAuthenticationProperties();
        BeanUtils.copyProperties(su.getLdap(), props);
        props.setMemberAttributeName("unknown");
        val ldapService = new SurrogateLdapAuthenticationService(factory, props, servicesManager);
        assertTrue(ldapService.getEligibleAccountsForSurrogateToProxy("casuser").isEmpty());
        ldapService.destroy();
    }
}
