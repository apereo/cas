package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for {@link DefaultX509AttributeExtractor}.
 * @author Hal Deadman
 * @since 6.4.0
 */
@Tag("X509")
public class DefaultX509AttributeExtractorTests {

    @SneakyThrows
    private X509Certificate getCertificate(final String certLocation) {
        return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(
                ResourceUtils.getRawResourceFrom(certLocation).getInputStream());
    }

    @Test
    public void verifyExtractedAttributes() {
        val extractor = new DefaultX509AttributeExtractor();
        val attributes = extractor.extractPersonAttributes(getCertificate("classpath:/x509-san-upn-resolver.crt"));
        assertEquals(CollectionUtils.wrapList("1.2.840.113549.1.1.11"), attributes.get("sigAlgOid"));
        assertEquals(CollectionUtils.wrapList(
                "EMAILADDRESS=test@somecompany.com, CN=Test User, OU=Some Department, O=Some Company, ST=District of Columbia, C=US"),
                attributes.get("subjectDn"));
        assertEquals(CollectionUtils.wrapList(
                "1.2.840.113549.1.9.1=#16147465737440736f6d65636f6d70616e792e636f6d,CN=Test User,OU=Some Department,O=Some Company,ST=District of Columbia,C=US"),
                attributes.get("subjectX500Principal"));
        assertEquals(CollectionUtils.wrapList(
                "EMAILADDRESS=ca@somecompany.com, CN=Test CA, OU=Some Department, O=Some Company, L=Washington, ST=District of Columbia, C=US"),
                attributes.get("issuerDn"));
        assertEquals(CollectionUtils.wrapList(
                "1.2.840.113549.1.9.1=#1612636140736f6d65636f6d70616e792e636f6d,CN=Test CA,OU=Some Department,O=Some Company,L=Washington,ST=District of Columbia,C=US"),
                attributes.get("issuerX500Principal"));
        assertEquals(CollectionUtils.wrapList("test@somecompany.com"), attributes.get("x509Rfc822Email"));
        assertEquals(CollectionUtils.wrapList("test-user@some-company-domain"), attributes.get("x509subjectUPN"));
    }

}
