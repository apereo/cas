package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link EDIPIX509AttributeExtractor}.
 * @author Hal Deadman
 * @since 6.4.0
 */
@Tag("X509")
class EDIPIX509AttributeExtractorTests {

    private static X509Certificate getCertificate(final String certLocation) throws Exception {
        return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(
                ResourceUtils.getRawResourceFrom(certLocation).getInputStream());
    }

    @Test
    void verifyExtractedAttributes() throws Throwable {
        val extractor = new EDIPIX509AttributeExtractor();
        val attributes = extractor.extractPersonAttributes(getCertificate("classpath:/x509-san-upn-resolver.crt"));
        assertNull(attributes.get("x509EDIPI"));
        val edipi = extractor.extractPersonAttributes(getCertificate("classpath:/edipi.cer")).get("x509EDIPI");
        assertEquals(edipi, CollectionUtils.wrapList("1234567890"));
    }

}
