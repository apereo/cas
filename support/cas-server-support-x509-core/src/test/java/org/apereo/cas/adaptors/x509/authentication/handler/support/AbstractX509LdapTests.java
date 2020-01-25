package org.apereo.cas.adaptors.x509.authentication.handler.support;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LdapTestUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.ldaptive.LdapAttribute;
import org.springframework.core.io.ClassPathResource;

/**
 * Parent class to help with testing x509 operations that deal with LDAP.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Ldap")
public abstract class AbstractX509LdapTests extends LdapIntegrationTestsOperations {

    private static final String DN = "CN=x509,ou=people,dc=example,dc=org";

    @SneakyThrows
    public static void bootstrap(final int port) {
        getLdapDirectory(port).populateEntries(new ClassPathResource("ldif/users-x509.ldif").getInputStream());
        populateCertificateRevocationListAttribute(port);
    }

    /**
     * Populate certificate revocation list attribute.
     * Dynamically set the attribute value to the crl content.
     * Encode it as base64 first. Doing this in the code rather
     * than in the ldif file to ensure the attribute can be populated
     * without dependencies on the classpath and or filesystem.
     *
     * @throws Exception the exception
     */
    private static void populateCertificateRevocationListAttribute(final int port) throws Exception {
        val col = getLdapDirectory(port).getLdapEntries();
        for (val ldapEntry : col) {
            if (ldapEntry.getDn().equals(DN)) {
                val userCA = new byte[1024];
                IOUtils.read(new ClassPathResource("userCA-valid.crl").getInputStream(), userCA);
                val value = EncodingUtils.encodeBase64ToByteArray(userCA);
                val attr = LdapAttribute.builder().name("certificateRevocationList").values(value).binary(true).build();
                LdapTestUtils.modifyLdapEntry(getLdapDirectory(port).getConnection(), ldapEntry, attr);
            }
        }
    }
}
