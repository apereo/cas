package org.apereo.cas.support.saml.util;

import lombok.val;

import static org.junit.Assert.*;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.junit.Test;

/**
 * Test cases for {@link SamlCompliantUniqueTicketIdGenerator}.
 * @author Scott Battaglia
 * @since 3.4.3
 */
@Slf4j
public class SamlCompliantUniqueTicketIdGeneratorTests extends AbstractOpenSamlTests {

    @Test
    public void verifySaml1Compliant() {
        val g = new SamlCompliantUniqueTicketIdGenerator("http://www.cnn.com");
        assertNotNull(g.getNewTicketId("TT"));
    }

    @Test
    public void verifySaml2Compliant() {
        val g = new SamlCompliantUniqueTicketIdGenerator("http://www.cnn.com");
        g.setSaml2compliant(true);
        assertNotNull(g.getNewTicketId("TT"));

    }
}
