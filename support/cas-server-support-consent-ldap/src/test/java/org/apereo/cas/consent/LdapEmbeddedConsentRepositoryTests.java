package org.apereo.cas.consent;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.util.junit.DisabledIfContinuousIntegration;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.SneakyThrows;
import org.springframework.test.context.TestPropertySource;

import static org.apereo.cas.util.LdapTestProperties.*;

/**
 * Unit tests for {@link LdapConsentRepository} class.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@TestPropertySource(properties = {
    "cas.consent.ldap.ldapUrl=ldap://localhost:1387",
    "cas.consent.ldap.useSsl=false",
    "cas.consent.ldap.baseDn=${ldap.peopleDn}",
    "cas.consent.ldap.searchFilter=cn={0}",
    "cas.consent.ldap.consentAttributeName=description",
    "ldap.test.resource=ldif/ldap-consent.ldif",
    "ldap.initLocal=true"

})
@DisabledIfContinuousIntegration
public class LdapEmbeddedConsentRepositoryTests extends BaseLdapConsentRepositoryTests {
    @Override
    @SneakyThrows
    public LDAPConnection getConnection() {
        return LdapIntegrationTestsOperations.getLdapDirectory(port()).getConnection();
    }
}
