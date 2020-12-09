package org.apereo.cas.consent;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LdapConsentRepository} class.
 *
 * @author Arnold Bergner
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.consent.ldap.ldap-url=ldap://localhost:10389",
    "cas.consent.ldap.base-dn=ou=people,dc=example,dc=org",
    "cas.consent.ldap.search-filter=cn={0}",
    "cas.consent.ldap.consent-attribute-name=description",
    "cas.consent.ldap.bind-dn=cn=Directory Manager",
    "cas.consent.ldap.bind-credential=password"
})
@EnabledIfPortOpen(port = 10389)
@Slf4j
@Tag("Ldap")
public class LdapConsentRepositoryTests extends BaseLdapConsentRepositoryTests {
    private static final int LDAP_PORT = 10389;

    @Autowired
    @Qualifier("consentLdapConnectionFactory")
    private ConnectionFactory consentLdapConnectionFactory;

    @BeforeAll
    public static void bootstrap() throws Exception {
        @Cleanup
        val localhost = new LDAPConnection("localhost", LDAP_PORT, "cn=Directory Manager", "password");
        val resource = new ClassPathResource("ldif/ldap-consent.ldif");
        LOGGER.debug("Populating LDAP entries from [{}]", resource);
        LdapIntegrationTestsOperations.populateEntries(localhost, resource.getInputStream(), "ou=people,dc=example,dc=org");
    }
    
    @Override
    @SneakyThrows
    public LDAPConnection getConnection() {
        val ldap = casProperties.getConsent().getLdap();
        return new LDAPConnection("localhost", LDAP_PORT,
            ldap.getBindDn(),
            ldap.getBindCredential());
    }

    @Test
    public void verifyConsentNotFound() {
        assertNotNull(consentLdapConnectionFactory);
        assertTrue(getRepository().findConsentDecisions("unknown-user").isEmpty());
    }

    @Test
    public void verifyNoConsent() {
        val ldap = casProperties.getConsent().getLdap();
        val factory = mock(ConnectionFactory.class);
        val repo = new LdapConsentRepository(factory, ldap);
        assertTrue(repo.findConsentDecisions().isEmpty());

        val decision = BUILDER.build(SVC, REG_SVC, "unknown", ATTR);
        assertNull(repo.storeConsentDecision(decision));

    }
}
