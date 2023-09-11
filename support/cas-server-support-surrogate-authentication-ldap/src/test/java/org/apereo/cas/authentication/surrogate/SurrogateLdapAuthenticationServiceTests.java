package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.SurrogateLdapAuthenticationConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateLdapAuthenticationProperties;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.Getter;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateLdapAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("LdapRepository")
@SpringBootTest(classes = {
    SurrogateLdapAuthenticationConfiguration.class,
    BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.surrogate.ldap.ldap-url=ldap://localhost:10389",
    "cas.authn.surrogate.ldap.base-dn=ou=surrogates,dc=example,dc=org",
    "cas.authn.surrogate.ldap.bind-dn=cn=Directory Manager",
    "cas.authn.surrogate.ldap.bind-credential=password",

    "cas.authn.surrogate.ldap.surrogate-search-filter=employeeType={surrogate}",
    "cas.authn.surrogate.ldap.surrogate-validation-filter=cn={surrogate}",

    "cas.authn.surrogate.ldap.search-filter=cn={user}",
    "cas.authn.surrogate.ldap.member-attribute-name=mail",
    "cas.authn.surrogate.ldap.member-attribute-value-regex=\\\\w+@example.org|\\\\*"
})
@Getter
@EnabledIfListeningOnPort(port = 10389)
class SurrogateLdapAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    private static final String USER = RandomUtils.randomAlphabetic(10);
    private static final String ADMIN_USER = RandomUtils.randomAlphabetic(10);

    private static final int LDAP_PORT = 10389;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
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

        val ldif = IOUtils.toString(new ClassPathResource("ldif/ldap-surrogate.ldif").getInputStream(), StandardCharsets.UTF_8)
            .replace("$user", USER).replace("$admin", ADMIN_USER);
        LdapIntegrationTestsOperations.populateEntries(
            localhost,
            new ByteArrayInputStream(ldif.getBytes(StandardCharsets.UTF_8)),
            "ou=surrogates,dc=example,dc=org");
    }

    @Override
    public String getTestUser() {
        return USER;
    }

    @Override
    public String getAdminUser() {
        return ADMIN_USER;
    }

    @Test
    void verifyFails() throws Throwable {
        val su = casProperties.getAuthn().getSurrogate();
        val factory = mock(ConnectionFactory.class);
        val ldapService = new SurrogateLdapAuthenticationService(factory, su.getLdap(), servicesManager);
        assertFalse(ldapService.canImpersonate(USER,
            CoreAuthenticationTestUtils.getPrincipal(), Optional.empty()));
        ldapService.destroy();
    }

    @Test
    void verifyNoAttr() throws Throwable {
        val su = casProperties.getAuthn().getSurrogate();
        val props = new SurrogateLdapAuthenticationProperties();
        BeanUtils.copyProperties(su.getLdap(), props);
        props.setMemberAttributeName("unknown");
        val ldapService = new SurrogateLdapAuthenticationService(surrogateLdapConnectionFactory, props, servicesManager);
        assertTrue(ldapService.getImpersonationAccounts(USER).isEmpty());
        ldapService.destroy();
    }

    @Test
    void verifyFailsProxying() throws Throwable {
        val su = casProperties.getAuthn().getSurrogate();
        val factory = mock(ConnectionFactory.class);
        val props = new SurrogateLdapAuthenticationProperties();
        BeanUtils.copyProperties(su.getLdap(), props);
        props.setMemberAttributeName("unknown");
        val ldapService = new SurrogateLdapAuthenticationService(factory, props, servicesManager);
        assertTrue(ldapService.getImpersonationAccounts(USER).isEmpty());
        ldapService.destroy();
    }
}
