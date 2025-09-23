package org.apereo.cas.adaptors.ldap;

import org.apereo.cas.util.LdapTestUtils;
import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.ldaptive.BindConnectionInitializer;
import org.springframework.core.io.ClassPathResource;
import java.io.InputStream;

/**
 * Base class for LDAP tests that provision and de-provision DIRECTORY data as part of test setup/tear-down.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@UtilityClass
public final class LdapIntegrationTestsOperations {
    /**
     * Populate entries.
     *
     * @param connection the c
     * @param rs         the rs
     * @param baseDn     the base dn
     * @throws Exception the exception
     */
    public static void populateEntries(final LDAPConnection connection, final InputStream rs, final String baseDn) throws Exception {
        val entries = LdapTestUtils.readLdif(rs, baseDn);
        LdapTestUtils.createLdapEntries(connection, entries, null);
    }

    /**
     * Populate entries.
     *
     * @param connection the c
     * @param rs         the rs
     * @param baseDn     the base dn
     * @param connInit   the connection initializer
     * @throws Exception the exception
     */
    public static void populateEntries(final LDAPConnection connection, final InputStream rs,
                                       final String baseDn, final BindConnectionInitializer connInit) throws Exception {
        LdapTestUtils.createLdapEntries(connection, LdapTestUtils.readLdif(rs, baseDn), connInit);
    }

    /**
     * Populate default entries.
     *
     * @param connection the connection
     * @param baseDn     the base dn
     * @throws Exception the exception
     */
    public static void populateDefaultEntries(final LDAPConnection connection, final String baseDn) throws Exception {
        populateEntries(connection, new ClassPathResource("ldif/users-groups.ldif").getInputStream(), baseDn);
    }
}
