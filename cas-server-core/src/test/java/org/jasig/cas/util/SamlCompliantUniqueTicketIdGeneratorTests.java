package org.jasig.cas.util;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.4.3
 */
public final class SamlCompliantUniqueTicketIdGeneratorTests extends TestCase {

    public void testSaml1Compliant() {
        final SamlCompliantUniqueTicketIdGenerator g = new SamlCompliantUniqueTicketIdGenerator("http://www.cnn.com");
        assertNotNull(g.getNewTicketId("TT"));
    }

    public void testSaml2Compliant() {
        final SamlCompliantUniqueTicketIdGenerator g = new SamlCompliantUniqueTicketIdGenerator("http://www.cnn.com");
        g.setSaml2compliant(true);
        assertNotNull(g.getNewTicketId("TT"));

    }
}
