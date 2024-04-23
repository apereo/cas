package org.apereo.cas.config;

import org.apereo.cas.adaptors.x509.BaseX509Tests;
import org.apereo.cas.adaptors.x509.authentication.CRLFetcher;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test makes sure the default X509 config loads without errors.
 * It purposely has minimal configuration and the defined crlFetcher is the default.
 *
 * @since 6.1.0
 */
@SpringBootTest(classes = BaseX509Tests.SharedTestConfiguration.class,
    properties = "cas.authn.x509.crl-fetcher=resource")
@Tag("X509")
class DefaultX509ConfigTests {

    @Autowired
    @Qualifier("crlFetcher")
    private CRLFetcher fetcher;

    /**
     * If there was a problem, this test would have failed to start up.
     * Confirm that config was loaded by ensuring bean was autowired.
     */
    @Test
    void verifyContextLoaded() throws Throwable {
        assertNotNull(fetcher);
    }
}
