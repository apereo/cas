package org.apereo.cas.consent;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit tests for {@link LdapConsentRepository} class.
 *
 * @author Arnold Bergner
 * @since 5.3.0
 */
@TestPropertySource(locations = "classpath:/ldapconsentci.properties")
@Slf4j
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class LdapContinuousIntegrationConsentRepositoryTests extends BaseLdapConsentRepositoryTests {
    private static final int LDAP_PORT = 10389;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    @SneakyThrows
    public LDAPConnection getConnection() {
        return new LDAPConnection("localhost", LDAP_PORT,
            casProperties.getConsent().getLdap().getBindDn(),
            casProperties.getConsent().getLdap().getBindCredential());
    }

    @BeforeClass
    @SneakyThrows
    public static void bootstrap() {
        final var localhost = new LDAPConnection("localhost", LDAP_PORT,
            "cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateEntries(
            localhost,
            new ClassPathResource("ldif/ldap-consent.ldif").getInputStream(),
            "ou=people,dc=example,dc=org");
    }
}
