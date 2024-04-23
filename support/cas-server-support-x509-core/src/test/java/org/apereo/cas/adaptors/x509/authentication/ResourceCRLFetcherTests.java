package org.apereo.cas.adaptors.x509.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.UrlResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ResourceCRLFetcherTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("X509")
class ResourceCRLFetcherTests {
    @Test
    void verifyFetchByUrl() throws Throwable {
        val fetcher = new ResourceCRLFetcher();
        assertNull(fetcher.fetch("https://httpbin.org/get"));
    }

    @Test
    void verifyFetchByResource() throws Throwable {
        val fetcher = new ResourceCRLFetcher();
        assertNull(fetcher.fetch(new UrlResource("https://httpbin.org/get")));
    }
}
