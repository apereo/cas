package org.apereo.cas.services;

import org.apereo.cas.services.domain.DefaultRegisteredServiceDomainExtractor;
import org.apereo.cas.services.domain.RegisteredServiceDomainExtractor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceDomainExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class DefaultRegisteredServiceDomainExtractorTests {

    @Test
    public void verifyDomains() {
        val ext = new DefaultRegisteredServiceDomainExtractor();
        assertEquals("www.example.org", ext.extract("https://www.example.org"));
        assertEquals("example.org", ext.extract("https://example.org"));
        assertEquals("example.org", ext.extract("http://example.org"));
        assertEquals(RegisteredServiceDomainExtractor.DOMAIN_DEFAULT, ext.extract("www.example.org"));
        assertEquals("somewhere.example.org",
            ext.extract("https://somewhere.example.org:1234/page/path"));
    }
}
