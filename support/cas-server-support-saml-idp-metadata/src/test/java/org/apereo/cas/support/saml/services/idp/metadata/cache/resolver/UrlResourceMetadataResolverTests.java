package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import module java.base;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UrlResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAMLMetadata")
class UrlResourceMetadataResolverTests {
    public static final String MDQ_URL = "https://mdq.incommon.org/entities/{0}";

    @TestPropertySource(properties = {
        "cas.authn.saml-idp.metadata.http.force-metadata-refresh=true",
        "cas.authn.saml-idp.metadata.file-system.location=file:/${#systemProperties['java.io.tmpdir']}/saml44"
    })
    @Nested
    class ForceMetadatRefreshTests extends BaseSamlIdPServicesTests {

        @Test
        void verifyResolverSupports() {
            try (val webServer = new MockWebServer(new ClassPathResource("sample-metadata.xml"))) {
                webServer.start();
                val resolver = getMetadataResolver();
                val service = new SamlRegisteredService();
                service.setMetadataLocation("http://localhost:%s".formatted(webServer.getPort()));
                assertTrue(resolver.supports(service));
                service.setMetadataLocation("classpath:sample-sp.xml");
                assertFalse(resolver.supports(service));
                service.setMetadataLocation(MDQ_URL);
                assertFalse(resolver.supports(service));
            }
        }

        @Test
        void verifyResolverFromBackup() throws Throwable {
            val service = new SamlRegisteredService();
            service.setName(RandomUtils.randomAlphabetic(12));
            service.setId(RandomUtils.nextInt());

            val resolver = getMetadataResolver();
            try (val webServer = new MockWebServer(new ClassPathResource("sample-metadata.xml"))) {
                webServer.start();
                service.setMetadataLocation("http://localhost:%s".formatted(webServer.getPort()));
                val results = resolver.resolve(service);
                assertFalse(results.isEmpty());
            }
        }

        @Test
        void verifyResolverResolves() throws Throwable {
            val resolver = getMetadataResolver();
            try (val webServer = new MockWebServer(new ClassPathResource("sample-metadata.xml"))) {
                webServer.start();

                val service = new SamlRegisteredService();
                service.setName(RandomUtils.randomAlphabetic(12));
                service.setId(RandomUtils.nextInt());
                service.setMetadataLocation("https://expired.badssl.com/,http://localhost:%s".formatted(webServer.getPort()));

                val results = resolver.resolve(service);
                assertFalse(results.isEmpty());
                assertTrue(resolver.isAvailable(service));
                assertFalse(resolver.supports(null));
            }
        }

        @Test
        void verifyResolverResolvesFailsAccess() {
            val resolver = getMetadataResolver();
            try (val webServer = new MockWebServer(new ClassPathResource("sample-metadata.xml"))) {
                webServer.start();
                val service = new SamlRegisteredService();
                service.setName(RandomUtils.randomAlphabetic(12));
                service.setId(RandomUtils.nextInt());
                service.setMetadataLocation("http://localhost:%s".formatted(webServer.getPort()));
                service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
                assertThrows(SamlException.class, () -> resolver.resolve(service));
            }
        }

        @Test
        void verifyResolverUnknownUrl() throws Throwable {
            val resolver = getMetadataResolver();
            val service = new SamlRegisteredService();
            service.setName(RandomUtils.randomAlphabetic(12));
            service.setId(RandomUtils.nextInt());
            service.setMetadataLocation("https://this-is-unknown.com:444");
            assertTrue(resolver.resolve(service).isEmpty());
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.saml-idp.metadata.http.force-metadata-refresh=false",
        "cas.authn.saml-idp.metadata.file-system.location=file:/${#systemProperties['java.io.tmpdir']}/saml445"
    })
    @Nested
    class DefaultMetadatRefreshTests extends BaseSamlIdPServicesTests {
        @Test
        void verifyResolverFromBackup() throws Throwable {
            val service = new SamlRegisteredService();
            service.setName(RandomUtils.randomAlphabetic(12));
            service.setId(RandomUtils.nextInt());

            val resolver = getMetadataResolver();
            try (val webServer = new MockWebServer(new ClassPathResource("sample-metadata.xml"))) {
                webServer.start();
                service.setMetadataLocation("http://localhost:%s".formatted(webServer.getPort()));
                val results = resolver.resolve(service);
                assertFalse(results.isEmpty());
            }

            val backupFile = resolver.getMetadataBackupFile(new UrlResource(service.getMetadataLocation()), service);
            FileUtils.writeByteArrayToFile(backupFile, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
            try (val webServer = new MockWebServer(new ClassPathResource("sample-metadata.xml"))) {
                webServer.start();
                service.setMetadataLocation("http://localhost:%s".formatted(webServer.getPort()));
                val finalResults = resolver.resolve(service);
                assertFalse(finalResults.isEmpty());
            }

            FileUtils.writeByteArrayToFile(backupFile, new ClassPathResource("metadata-invalid.xml").getInputStream().readAllBytes());
            try (val webServer = new MockWebServer(new ClassPathResource("sample-metadata.xml"))) {
                webServer.start();
                service.setMetadataLocation("http://localhost:%s".formatted(webServer.getPort()));
                val finalResults = resolver.resolve(service);
                assertFalse(finalResults.isEmpty());
            }
        }
    }
}
