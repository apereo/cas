package org.apereo.cas.consent;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningStandaloneCondition;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit tests for {@link LdapConsentRepository} class.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@TestPropertySource(properties = {
    "cas.consent.ldap.ldapUrl=ldap://localhost:1387",
    "cas.consent.ldap.useSsl=false",
    "cas.consent.ldap.baseDn=ou=people,dc=example,dc=org",
    "cas.consent.ldap.searchFilter=cn={0}",
    "cas.consent.ldap.consentAttributeName=description"
})
@ConditionalIgnore(condition = RunningStandaloneCondition.class)
public class LdapEmbeddedConsentRepositoryTests extends BaseLdapConsentRepositoryTests {
    private static final int LDAP_PORT = 1387;

    @BeforeAll
    public static void bootstrap() throws Exception {
        LdapIntegrationTestsOperations.initDirectoryServer(LDAP_PORT);
        LdapIntegrationTestsOperations.getLdapDirectory(LDAP_PORT).populateEntries(
            new ClassPathResource("ldif/ldap-consent.ldif").getInputStream());
    }

    @Override
    @SneakyThrows
    public LDAPConnection getConnection() {
        return LdapIntegrationTestsOperations.getLdapDirectory(LDAP_PORT).getConnection();
    }
}
