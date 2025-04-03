package org.apereo.cas.oidc.services;

import org.apereo.cas.config.CasJsonServiceRegistryAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("OIDCServices")
class OidcServicesManagerTests {

    @Nested
    @TestPropertySource(properties = "cas.service-registry.core.index-services=true")
    class IndexingServicesTests extends AbstractOidcTests {
        @Test
        void verifyParallelLoading() {
            for (var i = 0; i < 250; i++) {
                val redirectUri = "https://cas.oidc.org/%s".formatted(RandomUtils.randomAlphabetic(8));
                val oidcService = getOidcRegisteredService(UUID.randomUUID().toString(), redirectUri).setId(i);
                val casService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString()).setId(i);
                val oauthService = getOAuthRegisteredService(UUID.randomUUID().toString(), redirectUri).setId(i);
                servicesManager.save(oauthService, oidcService, casService);
            }
            IntStream.range(0, 100).parallel().forEach(__ ->
                assertFalse(servicesManager.getAllServices().isEmpty()));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.service-registry.json.watcher-enabled=false",
        "cas.service-registry.json.location=classpath:/services"
    })
    @ImportAutoConfiguration(CasJsonServiceRegistryAutoConfiguration.class)
    class JsonServicesTests extends AbstractOidcTests {

        private static final ClassPathResource RESOURCE = new ClassPathResource("services");

        @BeforeAll
        @AfterAll
        public static void prepTests() throws Exception {
            FileUtils.cleanDirectory(RESOURCE.getFile());
        }

        @Test
        void verifyLoadAndLookup() {
            val stream = getOidcRegisteredServiceStream();
            servicesManager.save(stream);
            val loadedServices = new ArrayList<>(servicesManager.load());

            val candidate = (OidcRegisteredService) loadedServices.getLast();
            val stopwatch = new StopWatch();
            stopwatch.start();

            val serviceToFind = webApplicationServiceFactory.createService(candidate.getServiceId());
            serviceToFind.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(candidate.getClientId()));

            val result = servicesManager.findServiceBy(serviceToFind);
            assertNotNull(result);
            assertEquals(candidate, result);

            stopwatch.stop();
            assertTrue(stopwatch.getDuration().toMillis() < 1000);
        }

        private static Stream<OidcRegisteredService> getOidcRegisteredServiceStream() {
            val count = 6000;
            return IntStream.range(0, count).mapToObj(i -> {
                val registeredService = new OidcRegisteredService();
                registeredService.setName(UUID.randomUUID().toString());
                registeredService.setServiceId("https://oidc.example.org/%s".formatted(i));
                registeredService.setClientId("Sample-" + UUID.randomUUID());
                registeredService.setClientSecret(UUID.randomUUID().toString());
                registeredService.setId(i);
                registeredService.setEvaluationOrder(i);
                return registeredService;
            });
        }
    }
}
