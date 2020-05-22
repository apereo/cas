package org.apereo.cas.consent;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit tests for {@link LdapConsentRepository} class.
 *
 * @author Arnold Bergner
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.consent.ldap.ldap-url=ldap://localhost:10389",
    "cas.consent.ldap.baseDn=ou=people,dc=example,dc=org",
    "cas.consent.ldap.searchFilter=cn={0}",
    "cas.consent.ldap.consentAttributeName=description",
    "cas.consent.ldap.bindDn=cn=Directory Manager",
    "cas.consent.ldap.bindCredential=password"
})
@EnabledIfPortOpen(port = 10389)
@Slf4j
@Tag("Ldap")
public class LdapContinuousIntegrationConsentRepositoryTests extends BaseLdapConsentRepositoryTests {
    private static final int LDAP_PORT = 10389;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    @SneakyThrows
    public static void bootstrap() {
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
}
