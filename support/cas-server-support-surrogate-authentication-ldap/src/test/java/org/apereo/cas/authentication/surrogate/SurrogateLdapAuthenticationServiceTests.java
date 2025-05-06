package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasSurrogateLdapAuthenticationAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateLdapAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("LdapRepository")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    CasSurrogateLdapAuthenticationAutoConfiguration.class,
    BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.surrogate.ldap[0].ldap-url=ldap://localhost:10389",
    "cas.authn.surrogate.ldap[0].base-dn=ou=surrogates,dc=example,dc=org",
    "cas.authn.surrogate.ldap[0].bind-dn=cn=Directory Manager",
    "cas.authn.surrogate.ldap[0].bind-credential=password",

    "cas.authn.surrogate.ldap[0].surrogate-search-filter=(&(cn={user})(employeeType={surrogate}))",
    "cas.authn.surrogate.ldap[0].surrogate-validation-filter=cn={surrogate}",

    "cas.authn.surrogate.ldap[0].search-filter=cn={user}",
    "cas.authn.surrogate.ldap[0].member-attribute-name=mail",
    "cas.authn.surrogate.ldap[0].member-attribute-value-regex=\\\\w+@example.org|\\\\*",

    "cas.authn.surrogate.ldap[1].ldap-url=ldap://localhost:10389",
    "cas.authn.surrogate.ldap[1].base-dn=ou=admins,dc=example,dc=org",
    "cas.authn.surrogate.ldap[1].bind-dn=cn=Directory Manager",
    "cas.authn.surrogate.ldap[1].bind-credential=password",

    "cas.authn.surrogate.ldap[1].surrogate-search-filter=(&(cn={user})(employeeType={surrogate}))",
    "cas.authn.surrogate.ldap[1].surrogate-validation-filter=cn={surrogate}",

    "cas.authn.surrogate.ldap[1].search-filter=cn={user}",
    "cas.authn.surrogate.ldap[1].member-attribute-name=mail",
    "cas.authn.surrogate.ldap[1].member-attribute-value-regex=\\\\w+@example.org|\\\\*"
})
@Getter
@EnabledIfListeningOnPort(port = 10389)
class SurrogateLdapAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    private static final String USER = RandomUtils.randomAlphabetic(10);
    private static final String ADMIN_USER = RandomUtils.randomAlphabetic(10);
    private static final String HELP_DESK = RandomUtils.randomAlphabetic(10);

    private static final int LDAP_PORT = 10389;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
    private SurrogateAuthenticationService service;

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
            .replace("$user", USER)
                .replace("$admin", ADMIN_USER)
                .replace("$helpdesk", HELP_DESK);
        LdapIntegrationTestsOperations.populateEntries(
            localhost,
            new ByteArrayInputStream(ldif.getBytes(StandardCharsets.UTF_8)),
            "ou=surrogates,dc=example,dc=org");
    }

    @Test
    void verifyUserDisabled() throws Throwable {
        assertTrue(getService().getImpersonationAccounts("banderson", Optional.empty()).isEmpty());
        assertTrue(getService().getImpersonationAccounts("nomail", Optional.empty()).isEmpty());
    }

    @Test
    void verifyProxyingDifferentOU() throws Throwable {
        val service = Optional.of(RegisteredServiceTestUtils.getService(UUID.randomUUID().toString()));
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.get().getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
        servicesManager.save(registeredService);

        val surrogateService = getService();
        assertTrue(surrogateService.canImpersonate(BANDERSON, CoreAuthenticationTestUtils.getPrincipal(USER), service));
        assertTrue(surrogateService.canImpersonate(BANDERSON, CoreAuthenticationTestUtils.getPrincipal(HELP_DESK), service));
        assertFalse(surrogateService.canImpersonate("nomail", CoreAuthenticationTestUtils.getPrincipal(HELP_DESK), service));
    }

    @Override
    public String getTestUser() {
        return USER;
    }

    @Override
    public String getAdminUser() {
        return ADMIN_USER;
    }
}
