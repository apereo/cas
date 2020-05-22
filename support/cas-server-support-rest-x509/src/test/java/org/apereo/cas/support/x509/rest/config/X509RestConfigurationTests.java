package org.apereo.cas.support.x509.rest.config;

import org.apereo.cas.adaptors.x509.config.X509CertificateExtractorConfiguration;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509RestConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    X509CertificateExtractorConfiguration.class,
    X509RestConfiguration.class
})
@Tag("X509")
public class X509RestConfigurationTests {
    @Autowired
    @Qualifier("x509RestMultipartBody")
    private RestHttpRequestCredentialFactory x509RestMultipartBody;

    @Autowired
    @Qualifier("x509RestRequestHeader")
    private RestHttpRequestCredentialFactory x509RestRequestHeader;

    @Test
    public void verifyOperation() {
        assertNotNull(x509RestMultipartBody);
        assertNotNull(x509RestRequestHeader);

    }
}
