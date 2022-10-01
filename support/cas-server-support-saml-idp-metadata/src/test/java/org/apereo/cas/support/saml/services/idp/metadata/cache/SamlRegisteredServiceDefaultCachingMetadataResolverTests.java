package org.apereo.cas.support.saml.services.idp.metadata.cache;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.ClasspathResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.MetadataQueryProtocolMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.UrlResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.DefaultSamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlRegisteredServiceDefaultCachingMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAMLMetadata")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.http.metadata-backup-location=file:${#systemProperties['java.io.tmpdir']}")
public class SamlRegisteredServiceDefaultCachingMetadataResolverTests extends BaseSamlIdPServicesTests {
    private static CriteriaSet getCriteriaFor(final String entityId) {
        val criteriaSet1 = new CriteriaSet();
        criteriaSet1.add(new EntityIdCriterion(entityId));
        criteriaSet1.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        return criteriaSet1;
    }

    @Test
    public void verifyAggregateCacheOverUrlResource() {
        val aggregateRegisteredService = new SamlRegisteredService();
        aggregateRegisteredService.setName("AggregateMetadata");
        aggregateRegisteredService.setId(1000);
        aggregateRegisteredService.setServiceId("https://.+");
        aggregateRegisteredService.setMetadataLocation("http://localhost:9191");

        val resolver = getResolver("PT1M");
        try (val webServer = new MockWebServer(9191, new ClassPathResource("aggregate-md.xml"), MediaType.APPLICATION_XML_VALUE)) {
            webServer.start();

            val criteriaSet1 = getCriteriaFor("https://issues.shibboleth.net/shibboleth");
            assertNotNull(resolver.resolve(aggregateRegisteredService, criteriaSet1));
            assertTrue(resolver.resolveIfPresent(aggregateRegisteredService, criteriaSet1).isPresent());

            val criteriaSet2 = getCriteriaFor("unknown-entity");
            assertThrows(SamlException.class, () -> resolver.resolve(aggregateRegisteredService, criteriaSet2));
            assertTrue(resolver.resolveIfPresent(aggregateRegisteredService, criteriaSet1).isPresent());
        }
    }

    @Test
    public void verifyCacheValidityForAggregates() {
        val criteriaSet1 = getCriteriaFor("https://issues.shibboleth.net/shibboleth");

        val aggregateRegisteredService = new SamlRegisteredService();
        aggregateRegisteredService.setName("AggregateMetadata");
        aggregateRegisteredService.setId(1000);
        aggregateRegisteredService.setServiceId("https://.+");
        aggregateRegisteredService.setMetadataLocation("classpath:aggregate-md.xml");

        val resolver = getResolver("PT1M");
        assertNotNull(resolver.resolve(aggregateRegisteredService, criteriaSet1));
        assertTrue(resolver.resolveIfPresent(aggregateRegisteredService, criteriaSet1).isPresent());

        val criteriaSet2 = getCriteriaFor("unknown-service-provider");
        assertThrows(SamlException.class, () -> resolver.resolve(aggregateRegisteredService, criteriaSet2));

        assertTrue(resolver.resolveIfPresent(aggregateRegisteredService, criteriaSet1).isPresent());

        val criteriaSet3 = getCriteriaFor("https://mfa-auth.dev.phenoapp.com/Saml2");
        assertNotNull(resolver.resolve(aggregateRegisteredService, criteriaSet3));
        assertTrue(resolver.resolveIfPresent(aggregateRegisteredService, criteriaSet3).isPresent());

        resolver.invalidate();
    }

    @Test
    public void verifyCacheValidityWithUnknownEntityId() {
        val criteriaSet = getCriteriaFor("https://carmenwiki.osu.edu/shibboleth");

        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setMetadataLocation("classpath:sample-sp.xml");

        val resolver = getResolver("PT1M");
        assertNotNull(resolver.resolve(service, criteriaSet));
        assertTrue(resolver.resolveIfPresent(service, criteriaSet).isPresent());

        val criteriaSet2 = getCriteriaFor("unknown-service-provider");
        assertThrows(SamlException.class, () -> resolver.resolve(service, criteriaSet2));

        assertFalse(resolver.resolveIfPresent(service, criteriaSet).isPresent());
        resolver.invalidate();
    }

    @Test
    public void verifyRetryableOpWithFailure() {
        val criteriaSet = getCriteriaFor("urn:app.e2ma.net");

        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId("urn:.+");
        service.setMetadataLocation("classpath:metadata-invalid.xml");

        val resolver = getResolver("PT5S");
        assertThrows(SamlException.class, () -> resolver.resolve(service, criteriaSet));
        resolver.invalidate();
    }

    @Test
    public void verifyRetryableOp() {

        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId(".+");
        service.setMetadataLocation("classpath:sample-sp.xml");

        val resolver = getResolver("PT5S");
        val criteriaSet1 = getCriteriaFor("https://carmenwiki.osu.edu/shibboleth");
        assertNotNull(resolver.resolve(service, criteriaSet1));

        val criteriaSet2 = getCriteriaFor("unknown-service-provider");
        assertThrows(SamlException.class, () -> resolver.resolve(service, criteriaSet2));

        assertFalse(resolver.resolveIfPresent(service, criteriaSet1).isPresent());
        resolver.invalidate();
    }

    @Test
    public void verfifyAggregatedCacheLoading() throws Exception {
        val resolver = getResolver("PT5M");

        val service1 = getSamlRegisteredService(1, ".*", "classpath:aggregate-md.xml");
        assertNotNull(resolver.resolve(service1, getCriteriaFor("https://issues.shibboleth.net/shibboleth")));
        assertNotNull(resolver.resolve(service1, getCriteriaFor("https://mfa-auth.dev.phenoapp.com/Saml2")));
        assertNotNull(resolver.resolve(service1, getCriteriaFor("https://gitlab.com")));
        assertEquals(1, resolver.getCacheStatistics().loadSuccessCount());

        for (int i = 0; i < 5; i++) {
            val resource = new ClassPathResource("placeholder-sp.xml");
            val data = FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8)
                .replace("%ENTITY_ID%", "https://gitlab.com");
            val mdFile = Files.createTempFile("samplesp", ".xml").toFile();
            FileUtils.writeStringToFile(mdFile, data, StandardCharsets.UTF_8);
            val service2 = getSamlRegisteredService(i, ".*", "file://" + mdFile.getAbsolutePath());
            assertNotNull(resolver.resolve(service2, getCriteriaFor("https://gitlab.com")));
            assertEquals(1, resolver.getCacheStatistics().loadSuccessCount());
            assertTrue(mdFile.delete());
        }

    }

    @Test
    public void verifyMissingMetadataInMDQ() {
        val criteriaSet1 = getCriteriaFor("https://shib-sp-test-preprod.dartmouth.edu/shibboleth");
        val service = getSamlRegisteredService(1, ".*", "https://mdq.incommon.org/entities/{0}");
        val resolver = getResolver("PT5M");
        assertThrows(SamlException.class, () -> resolver.resolve(service, criteriaSet1));
    }

    @Test
    public void verifyDynamicMetadata() {
        val criteriaSet1 = getCriteriaFor("urn:mace:incommon:internet2.edu");

        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId(".+");
        service.setMetadataLocation("https://mdq.incommon.org/entities/{0}");

        val resolver = getResolver("PT5S");

        assertNotNull(resolver.resolve(service, criteriaSet1));
        val stats1 = resolver.getCacheStatistics();
        assertEquals(1, stats1.missCount());
        assertEquals(1, stats1.loadSuccessCount());
        assertEquals(0, stats1.hitCount());

        assertNotNull(resolver.resolve(service, criteriaSet1));
        val stats2 = resolver.getCacheStatistics();
        assertEquals(1, stats2.missCount());
        assertEquals(1, stats2.loadSuccessCount());
        assertEquals(0, stats2.hitCount());

        val criteriaSet2 = getCriteriaFor("https://vbushib.einsteinmed.org/idp/");

        assertNotNull(resolver.resolve(service, criteriaSet2));
        val stats3 = resolver.getCacheStatistics();
        assertEquals(2, stats3.missCount());
        assertEquals(2, stats3.loadSuccessCount());
        assertEquals(0, stats3.hitCount());
    }

    private SamlRegisteredServiceDefaultCachingMetadataResolver getResolver(final String duration) {
        val resolutionPlan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();
        val props = casProperties.getAuthn().getSamlIdp();
        resolutionPlan.registerMetadataResolver(
            new UrlResourceMetadataResolver(props, openSamlConfigBean));
        resolutionPlan.registerMetadataResolver(
            new MetadataQueryProtocolMetadataResolver(props, openSamlConfigBean));
        resolutionPlan.registerMetadataResolver(
            new ClasspathResourceMetadataResolver(props, openSamlConfigBean));
        val cacheLoader = new SamlRegisteredServiceMetadataResolverCacheLoader(openSamlConfigBean, httpClient, resolutionPlan);
        casProperties.getAuthn().getSamlIdp().getMetadata().getCore().setCacheExpiration(duration);
        return new SamlRegisteredServiceDefaultCachingMetadataResolver(casProperties, cacheLoader, openSamlConfigBean);
    }
}
