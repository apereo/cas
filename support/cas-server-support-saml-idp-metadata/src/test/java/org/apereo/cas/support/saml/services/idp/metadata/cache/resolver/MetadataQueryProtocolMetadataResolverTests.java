package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import module java.base;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MetadataQueryProtocolMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAMLMetadata")
class MetadataQueryProtocolMetadataResolverTests extends BaseSamlIdPServicesTests {
    private String fileSystemMetadataPath;

    @BeforeEach
    void setup() throws Throwable {
        val file = new File(FileUtils.getTempDirectory(), "metadata-%s".formatted(RandomUtils.randomAlphanumeric(6)));
        if (!file.mkdirs()) {
            fail(() -> "Unable to create directory " + file);
        }
        fileSystemMetadataPath = file.getCanonicalPath();
    }

    @Test
    void verifyResolverSupports() {
        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(fileSystemMetadataPath);
        val resolver = new MetadataQueryProtocolMetadataResolver(httpClient, props, openSamlConfigBean);
        val service = new SamlRegisteredService();
        service.setMetadataLocation("http://www.testshib.org/metadata/testshib-providers.xml");
        assertFalse(resolver.supports(service));
        service.setMetadataLocation("http://mdq-preview.incommon.org/entities/{0}");
        assertTrue(resolver.supports(service));
    }

    @Test
    void verifyResolverResolves() throws Throwable {
        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(fileSystemMetadataPath);
        val resolver = new MetadataQueryProtocolMetadataResolver(httpClient, props, openSamlConfigBean);
        val service = new SamlRegisteredService();
        service.setId(RandomUtils.nextLong());
        service.setName(RandomUtils.randomAlphabetic(12));
        service.setMetadataLocation("http://mdq.incommon.org/entities/{0}");
        service.setServiceId("https://webauth.cmc.edu/idp/shibboleth");
        val results = resolver.resolve(service);
        assertFalse(results.isEmpty());
    }

    @Test
    void verifyResolverWithMultipleURLs() throws Throwable {
        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(fileSystemMetadataPath);
        val resolver = new MetadataQueryProtocolMetadataResolver(httpClient, props, openSamlConfigBean);
        val service = new SamlRegisteredService();
        service.setId(RandomUtils.nextLong());
        service.setName(RandomUtils.randomAlphabetic(12));
        service.setMetadataLocation("http://mdq.ukfederation.org.uk/entities/{0},http://mdq.incommon.org/entities/{0}");
        service.setServiceId("https://webauth.cmc.edu/idp/shibboleth");
        val results = resolver.resolve(service);
        assertFalse(results.isEmpty());
        assertTrue(resolver.isAvailable(service));
        assertTrue(resolver.supports(service));
    }

    @Test
    void verifyResolverFails() throws Throwable {
        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(fileSystemMetadataPath);
        val resolver = new MetadataQueryProtocolMetadataResolver(httpClient, props, openSamlConfigBean);
        val service = new SamlRegisteredService();
        service.setId(RandomUtils.nextLong());
        service.setName(RandomUtils.randomAlphabetic(12));
        service.setMetadataLocation("https://github1234.com/entities/{0}");
        service.setServiceId("https://webauth.cmc.edu/idp/shibboleth");
        assertThrows(SamlException.class, () -> resolver.resolve(service));

        service.setMetadataLocation("https://github.com/entities/{0}");
        assertTrue(resolver.resolve(service).isEmpty());
    }
}
