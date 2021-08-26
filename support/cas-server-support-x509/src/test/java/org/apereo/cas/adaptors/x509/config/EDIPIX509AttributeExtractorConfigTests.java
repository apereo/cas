package org.apereo.cas.adaptors.x509.config;

import org.apereo.cas.adaptors.x509.BaseX509Tests;
import org.apereo.cas.adaptors.x509.authentication.principal.EDIPIX509AttributeExtractor;
import org.apereo.cas.adaptors.x509.authentication.principal.X509AttributeExtractor;
import org.apereo.cas.util.ResourceUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;

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
    public void verifyCorrectX509AttributeExtractorLoaded() throws IOException, CertificateException {
        assertNotNull(x509AttributeExtractor);
        val certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(
                ResourceUtils.getResourceFrom("classpath:/edipi.cer").getInputStream());
        assertTrue(x509AttributeExtractor.extractPersonAttributes(certificate).containsKey("x509EDIPI"));
    }
}
