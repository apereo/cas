package org.apereo.cas.consent;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.LdapTest;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.apereo.cas.util.LdapTestProperties.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.*;

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
    "cas.consent.ldap.bindCredential=password",
    "ldap.test.resource=ldif/ldap-consent.ldif"
    })
@DirtiesContext(classMode = BEFORE_CLASS)
@EnabledIfContinuousIntegration
public class LdapContinuousIntegrationConsentRepositoryTests extends BaseLdapConsentRepositoryTests implements LdapTest {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    @SneakyThrows
    public LDAPConnection getConnection() {
        val ldap = casProperties.getConsent().getLdap();
        return new LDAPConnection(host(), port(), ldap.getBindDn(), ldap.getBindCredential());
    }
}
