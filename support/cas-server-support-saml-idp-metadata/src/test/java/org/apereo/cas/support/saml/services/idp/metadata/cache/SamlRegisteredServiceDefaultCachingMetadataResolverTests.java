package org.apereo.cas.support.saml.services.idp.metadata.cache;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.ClasspathResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.MetadataQueryProtocolMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.DefaultSamlRegisteredServiceMetadataResolutionPlan;

import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlRegisteredServiceDefaultCachingMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML")
public class SamlRegisteredServiceDefaultCachingMetadataResolverTests extends BaseSamlIdPServicesTests {
    @Test
    public void verifyRetryableOpWithFailure() {
        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion("urn:app.e2ma.net"));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));

        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId("urn:.+");
        service.setMetadataLocation("classpath:metadata-invalid.xml");

        val resolutionPlan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();
        resolutionPlan.registerMetadataResolver(
            new ClasspathResourceMetadataResolver(casProperties.getAuthn().getSamlIdp(), openSamlConfigBean));
        val cacheLoader = new SamlRegisteredServiceMetadataResolverCacheLoader(openSamlConfigBean, httpClient, resolutionPlan);
        val resolver = new SamlRegisteredServiceDefaultCachingMetadataResolver(Duration.ofSeconds(5), cacheLoader, openSamlConfigBean);
        assertThrows(SamlException.class, () -> resolver.resolve(service, criteriaSet));
        resolver.invalidate();
    }

    @Test
    public void verifyRetryableOp() {
        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion("https://carmenwiki.osu.edu/shibboleth"));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));

        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId(".+");
        service.setMetadataLocation("classpath:sample-sp.xml");

        val resolutionPlan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();
        resolutionPlan.registerMetadataResolver(
            new ClasspathResourceMetadataResolver(casProperties.getAuthn().getSamlIdp(), openSamlConfigBean));
        val cacheLoader = new SamlRegisteredServiceMetadataResolverCacheLoader(openSamlConfigBean, httpClient, resolutionPlan);
        val resolver = new SamlRegisteredServiceDefaultCachingMetadataResolver(Duration.ofSeconds(5), cacheLoader, openSamlConfigBean);
        assertNotNull(resolver.resolve(service, criteriaSet));
        resolver.invalidate();
    }

    @Test
    public void verifyDynamicMetadata() {
        val criteriaSet1 = new CriteriaSet();
        criteriaSet1.add(new EntityIdCriterion("urn:mace:incommon:internet2.edu"));
        criteriaSet1.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));

        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId(".+");
        service.setMetadataLocation("https://mdq.incommon.org/entities/{0}");

        val resolutionPlan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();
        resolutionPlan.registerMetadataResolver(
            new MetadataQueryProtocolMetadataResolver(casProperties.getAuthn().getSamlIdp(), openSamlConfigBean));
        val cacheLoader = new SamlRegisteredServiceMetadataResolverCacheLoader(openSamlConfigBean, httpClient, resolutionPlan);
        val resolver = new SamlRegisteredServiceDefaultCachingMetadataResolver(Duration.ofSeconds(5), cacheLoader, openSamlConfigBean);

        assertNotNull(resolver.resolve(service, criteriaSet1));
        val stats1 = resolver.getCacheStatistics();
        assertEquals(1, stats1.missCount());
        assertEquals(1, stats1.loadSuccessCount());
        assertEquals(0, stats1.hitCount());
        
        assertNotNull(resolver.resolve(service, criteriaSet1));
        val stats2 = resolver.getCacheStatistics();
        assertEquals(1, stats2.missCount());
        assertEquals(1, stats2.loadSuccessCount());
        assertEquals(1, stats2.hitCount());

        val criteriaSet2 = new CriteriaSet();
        criteriaSet2.add(new EntityIdCriterion("https://vbushib.einsteinmed.org/idp/"));
        criteriaSet2.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));

        assertNotNull(resolver.resolve(service, criteriaSet2));
        val stats3 = resolver.getCacheStatistics();
        assertEquals(2, stats3.missCount());
        assertEquals(2, stats3.loadSuccessCount());
        assertEquals(1, stats3.hitCount());
    }
}
