package org.apereo.cas.adaptors.x509;

import org.apereo.cas.adaptors.x509.authentication.RequestHeaderX509CertificateExtractor;
import org.apereo.cas.adaptors.x509.authentication.X509CertificateExtractor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RequestHeaderX509CertificateExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("X509")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseX509Tests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class RequestHeaderX509CertificateExtractorTests {
    @Autowired
    @Qualifier("x509CertificateExtractor")
    private X509CertificateExtractor x509CertificateExtractor;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyNullHeader() {
        val request = new MockHttpServletRequest();
        request.addHeader(casProperties.getAuthn().getX509().getSslHeaderName(), "(null)");
        assertNull(x509CertificateExtractor.extract(request));
    }

    @Test
    void verifyBadHeaderLength() {
        val request = new MockHttpServletRequest();
        request.addHeader(casProperties.getAuthn().getX509().getSslHeaderName(), "header-value");
        assertNull(x509CertificateExtractor.extract(request));
    }

    @Test
    void verifyBadHeaderLength2() {
        val cert = RequestHeaderX509CertificateExtractor.X509_HEADER;
        val request = new MockHttpServletRequest();
        request.addHeader(casProperties.getAuthn().getX509().getSslHeaderName(), cert);
        assertNull(x509CertificateExtractor.extract(request));
    }

    @Test
    void verifyBadHeaderLength3() {
        val cert = RequestHeaderX509CertificateExtractor.X509_HEADER + RequestHeaderX509CertificateExtractor.X509_FOOTER;
        val request = new MockHttpServletRequest();
        request.addHeader(casProperties.getAuthn().getX509().getSslHeaderName(), cert);
        assertNull(x509CertificateExtractor.extract(request));
    }

    @Test
    void verifyBadHeaderLength4() {
        val cert = RequestHeaderX509CertificateExtractor.X509_HEADER + ' ' + RequestHeaderX509CertificateExtractor.X509_FOOTER;
        val request = new MockHttpServletRequest();
        request.addHeader(casProperties.getAuthn().getX509().getSslHeaderName(), cert);
        assertNull(x509CertificateExtractor.extract(request));
    }

    @Test
    void verifyBadHeader() {
        val cert = RequestHeaderX509CertificateExtractor.X509_HEADER + "\nwhatever\n" + RequestHeaderX509CertificateExtractor.X509_FOOTER;
        val request = new MockHttpServletRequest();
        request.addHeader(casProperties.getAuthn().getX509().getSslHeaderName(), cert);
        assertNull(x509CertificateExtractor.extract(request));
    }

}
