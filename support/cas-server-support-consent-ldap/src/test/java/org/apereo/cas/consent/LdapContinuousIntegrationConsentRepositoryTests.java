package org.apereo.cas.consent;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.LdapTest;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
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
    "cas.consent.ldap.ldapUrl=${ldap.url}",
    "cas.consent.ldap.useSsl=false",
    "cas.consent.ldap.baseDn=${ldap.peopleDn}",
    "cas.consent.ldap.searchFilter=cn={0}",
    "cas.consent.ldap.consentAttributeName=description",
    "cas.consent.ldap.bindDn=${ldap.bindDn}",
    "cas.consent.ldap.bindCredential=password"
    })
@EnabledIfContinuousIntegration
public class LdapContinuousIntegrationConsentRepositoryTests extends BaseLdapConsentRepositoryTests implements LdapTest {
    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    @SneakyThrows
    public static void bootstrap() {
        @Cleanup
        val localhost = new LDAPConnection(HOST, PORT, BIND_DN, BIND_PASS);
        LdapIntegrationTestsOperations.populateEntries(localhost, new ClassPathResource("ldif/ldap-consent.ldif").getInputStream(), PEOPLE_DN);
    }

    @Override
    @SneakyThrows
    public LDAPConnection getConnection() {
        return new LDAPConnection("localhost", PORT, casProperties.getConsent().getLdap().getBindDn(),
            casProperties.getConsent().getLdap().getBindCredential());
    }
}
