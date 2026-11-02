package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import module java.base;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
        void verifyServicesSharingOneLocationShareOneBackupFile() throws Throwable {
            val resolver = getMetadataResolver();
            try (val webServer = new MockWebServer(new ClassPathResource("sample-metadata.xml"))) {
                webServer.start();
                val location = "http://localhost:%s".formatted(webServer.getPort());

                val serviceOne = new SamlRegisteredService();
                serviceOne.setName(RandomUtils.randomAlphabetic(12));
                serviceOne.setId(RandomUtils.nextInt());
                serviceOne.setMetadataLocation(location);

                val serviceTwo = new SamlRegisteredService();
                serviceTwo.setName(RandomUtils.randomAlphabetic(12));
                serviceTwo.setId(RandomUtils.nextInt());
                serviceTwo.setMetadataLocation(location);

                assertFalse(resolver.resolve(serviceOne).isEmpty());
                assertFalse(resolver.resolve(serviceTwo).isEmpty());

                val backupFile = resolver.getMetadataBackupFile(serviceOne, new CriteriaSet());
                assertEquals(backupFile, resolver.getMetadataBackupFile(serviceTwo, new CriteriaSet()));
                assertTrue(backupFile.exists());
                assertTrue(FileUtils.listFiles(backupFile.getParentFile(), new String[]{"tmp"}, false).isEmpty(),
                    "Metadata backup directory must not retain temporary files");
            }
        }

        @Test
        void verifyResolverFallsBackToBackupWhenSourceIsUnreachable() throws Throwable {
            val service = new SamlRegisteredService();
            service.setName(RandomUtils.randomAlphabetic(12));
            service.setId(RandomUtils.nextInt());

            val resolver = getMetadataResolver();
            try (val webServer = new MockWebServer(new ClassPathResource("sample-metadata.xml"))) {
                webServer.start();
                service.setMetadataLocation("http://localhost:%s".formatted(webServer.getPort()));
                assertFalse(resolver.resolve(service).isEmpty());
            }
            val backupFile = resolver.getMetadataBackupFile(service, new CriteriaSet());
            assertTrue(backupFile.exists(), "Metadata backup file must survive a forced refresh");
            assertFalse(resolver.resolve(service).isEmpty(),
                "Metadata backup file must carry the service while the metadata source is unreachable");
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
            val error = assertThrows(SamlException.class, () -> resolver.resolve(service));
            assertTrue(error.getMessage().contains("https://this-is-unknown.com:444"));
        }

        @Test
        void verifyConditionalRequestIsServedFromBackup() throws Throwable {
            val resolver = getMetadataResolver();
            val service = new SamlRegisteredService();
            service.setName(RandomUtils.randomAlphabetic(12));
            service.setId(RandomUtils.nextInt());
            val etag = "\"%s\"".formatted(UUID.randomUUID().toString());
            val conditionalHeader = new AtomicReference<String>();
            val metadata = new ClassPathResource("sample-metadata.xml").getContentAsString(StandardCharsets.UTF_8);
            try (val webServer = new MockWebServer(new ClassPathResource("sample-metadata.xml"))) {
                webServer.headers(Map.of(HttpHeaders.ETAG, etag));
                webServer.headersConsumer(headers -> {
                    val given = headers.get(HttpHeaders.IF_NONE_MATCH);
                    conditionalHeader.set(given);
                    val notModified = etag.equals(given);
                    webServer.responseStatus(notModified ? HttpStatus.NOT_MODIFIED : HttpStatus.OK);
                    webServer.responseBody(notModified ? StringUtils.EMPTY : metadata);
                });
                webServer.start();
                service.setMetadataLocation("http://localhost:%s".formatted(webServer.getPort()));
                assertFalse(resolver.resolve(service).isEmpty());
                assertNull(conditionalHeader.get());

                val backupFile = resolver.getMetadataBackupFile(service, new CriteriaSet());
                assertTrue(resolver.getMetadataValidatorsFile(backupFile).exists());
                FileUtils.writeByteArrayToFile(backupFile, new ClassPathResource("sample-sp.xml").getInputStream().readAllBytes());
                assertFalse(resolver.resolve(service).isEmpty());
                assertNull(conditionalHeader.get(), "Validators must not be used for a backup file holding another document");

                assertTrue(backupFile.setLastModified(Instant.now(Clock.systemUTC()).minus(Duration.ofDays(30)).toEpochMilli()));
                assertEquals(Boolean.TRUE, resolver.describe(service, new CriteriaSet()).get("metadataBackupStale"));
                assertFalse(resolver.resolve(service).isEmpty());
                assertEquals(etag, conditionalHeader.get());
                assertEquals(3, webServer.getRequestCount());
                assertEquals(Boolean.FALSE, resolver.describe(service, new CriteriaSet()).get("metadataBackupStale"),
                    "A source answering that the metadata has not changed confirms the backup file");
            }
        }

        @Test
        void verifyStaleBackupIsReportedAndServed() throws Throwable {
            val resolver = getMetadataResolver();
            val service = new SamlRegisteredService();
            service.setName(RandomUtils.randomAlphabetic(12));
            service.setId(RandomUtils.nextInt());
            try (val webServer = new MockWebServer(new ClassPathResource("sample-metadata.xml"))) {
                webServer.start();
                service.setMetadataLocation("http://localhost:%s".formatted(webServer.getPort()));
                assertFalse(resolver.resolve(service).isEmpty());
            }
            val backupFile = resolver.getMetadataBackupFile(service, new CriteriaSet());
            var details = resolver.describe(service, new CriteriaSet());
            assertEquals(backupFile.getCanonicalPath(), details.get("metadataBackupFile"));
            assertEquals(Boolean.TRUE, details.get("metadataBackupExists"));
            assertEquals(Boolean.FALSE, details.get("metadataBackupStale"));

            assertTrue(backupFile.setLastModified(Instant.now(Clock.systemUTC()).minus(Duration.ofDays(8)).toEpochMilli()));
            details = resolver.describe(service, new CriteriaSet());
            assertEquals(Boolean.TRUE, details.get("metadataBackupStale"));
            assertTrue(Duration.parse(details.get("metadataBackupAge").toString()).toDays() >= 8);
            assertFalse(resolver.resolve(service).isEmpty(), "A stale metadata backup file must still be served");
        }

        @Test
        void verifyEveryLocationIsTriedBeforeBackup() throws Throwable {
            val resolver = getMetadataResolver();
            val service = new SamlRegisteredService();
            service.setName(RandomUtils.randomAlphabetic(12));
            service.setId(RandomUtils.nextInt());
            try (val failingServer = new MockWebServer(new ClassPathResource("sample-metadata.xml"), HttpStatus.NOT_FOUND);
                 val workingServer = new MockWebServer(new ClassPathResource("sample-metadata.xml"))) {
                failingServer.start();
                workingServer.start();
                service.setMetadataLocation("http://localhost:%s,http://localhost:%s".formatted(failingServer.getPort(), workingServer.getPort()));
                val backupFile = resolver.getMetadataBackupFile(service, new CriteriaSet());
                FileUtils.writeByteArrayToFile(backupFile, new ClassPathResource("sample-sp.xml").getInputStream().readAllBytes());

                val results = resolver.resolve(service);
                assertEquals(1, results.size());
                val entity = results.iterator().next().resolveSingle(new CriteriaSet(new EntityIdCriterion("https://mockypost.io")));
                assertNotNull(entity, "A working mirror must be preferred over the metadata backup file");
                assertEquals(1, failingServer.getRequestCount());
                assertEquals(1, workingServer.getRequestCount());
            }
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

            val backupFile = resolver.getMetadataBackupFile(service, new CriteriaSet());
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
