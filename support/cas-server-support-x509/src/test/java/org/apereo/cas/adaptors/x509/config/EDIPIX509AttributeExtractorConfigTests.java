package org.apereo.cas.adaptors.x509.config;

import org.apereo.cas.adaptors.x509.BaseX509Tests;
import org.apereo.cas.adaptors.x509.authentication.principal.EDIPIX509AttributeExtractor;
import org.apereo.cas.adaptors.x509.authentication.principal.X509AttributeExtractor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Testing configuration for {@link X509AttributeExtractor} and {@link EDIPIX509AttributeExtractor}.
 * @author Hal Deadman
 * @since 6.4.0
 */
@SpringBootTest(classes = BaseX509Tests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.x509.principal-type=SUBJECT_DN",
        "cas.authn.x509.cn-edipi.extract-edipi-as-attribute=true"
    })
@Tag("X509")
public class EDIPIX509AttributeExtractorConfigTests {

    @Autowired
    @Qualifier("x509AttributeExtractor")
    private X509AttributeExtractor x509AttributeExtractor;

    /**
     * If there was a problem, this test would have failed to start up.
     * Confirm that non-default bean loaded per properties.
     */
    @Test
    public void verifyCorrectX509AttributeExtractorLoaded() {
        assertNotNull(x509AttributeExtractor);
        assertEquals(EDIPIX509AttributeExtractor.class, x509AttributeExtractor.getClass());
    }
}
