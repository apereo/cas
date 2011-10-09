package org.jasig.cas.util;

import junit.framework.TestCase;

/**
 * @author Daniel Frett
 * @version $Revision$ $Date$
 * @since 3.4.11
 */
public final class LdapUtilTests extends TestCase {

     public void testEncoding() {
        final String filter = "cn=%u";
        assertEquals("cn=test\\+user@example.com", LdapUtils.getFilterWithValues(filter, "test+user@example.com"));
    }
}
