package org.apereo.cas.support.saml.services.idp.metadata.cache;

import module java.base;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlRegisteredServiceCacheKeyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML")
class SamlRegisteredServiceCacheKeyTests {
    @Test
    void verifyCacheKeyByMetadataLocation() {
        val entityId = "https://carmenwiki.osu.edu/shibboleth";

        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion(entityId));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));

        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId(".+");
        service.setMetadataLocation("classpath:sample-sp.xml");

        val results = new SamlRegisteredServiceCacheKey(service, criteriaSet);
        assertNotNull(results.getId());
        assertNotNull(results.getRegisteredService());
        assertNotNull(results.getCriteriaSet());
        assertTrue(results.getCacheKey().startsWith(service.getId() + SamlRegisteredServiceCacheKey.KEY_SEPARATOR
            + service.getMetadataLocation() + SamlRegisteredServiceCacheKey.KEY_SEPARATOR));
    }

    @Test
    void verifyCacheKeyDynamicMetadata() {
        val criteriaSet = new CriteriaSet();
        val entityIdCriterion = new EntityIdCriterion("https://carmenwiki.osu.edu/shibboleth");
        criteriaSet.add(entityIdCriterion);
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));

        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId(".+");
        service.setMetadataLocation("https://mdq.something.net/entities/{0}");

        val result1 = new SamlRegisteredServiceCacheKey(service, criteriaSet);
        assertNotNull(result1.getId());
        assertNotNull(result1.toString());
        assertTrue(result1.getCacheKey().startsWith(service.getId() + SamlRegisteredServiceCacheKey.KEY_SEPARATOR
            + service.getMetadataLocation() + SamlRegisteredServiceCacheKey.KEY_SEPARATOR + entityIdCriterion.getEntityId()
            + SamlRegisteredServiceCacheKey.KEY_SEPARATOR));

        val result2 = new SamlRegisteredServiceCacheKey(service, criteriaSet);
        assertEquals(result1, result2);
    }

    @Test
    void verifyCacheKeyNoEntityIdCriteria() {
        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));

        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId(".+");
        service.setMetadataLocation("https://mdq.something.net/entities/{0}");

        val results = new SamlRegisteredServiceCacheKey(service, criteriaSet);
        assertNotNull(results.getId());
        assertTrue(results.getCacheKey().startsWith(service.getId() + SamlRegisteredServiceCacheKey.KEY_SEPARATOR
            + service.getMetadataLocation() + SamlRegisteredServiceCacheKey.KEY_SEPARATOR + service.getServiceId()
            + SamlRegisteredServiceCacheKey.KEY_SEPARATOR));
    }

    @Test
    void verifyCacheKeyIsScopedToRegisteredService() {
        val criteriaSet = new CriteriaSet(new EntityIdCriterion("https://shared.example.org"));
        val firstService = new SamlRegisteredService();
        firstService.setId(1000);
        firstService.setMetadataLocation("classpath:sample-sp.xml");

        val secondService = new SamlRegisteredService();
        secondService.setId(2000);
        secondService.setMetadataLocation(firstService.getMetadataLocation());

        val firstKey = new SamlRegisteredServiceCacheKey(firstService, criteriaSet);
        val secondKey = new SamlRegisteredServiceCacheKey(secondService, criteriaSet);
        assertNotEquals(firstKey, secondKey);
    }

    @Test
    void verifyCacheKeyForMetadataStoreIsScopedToEntity() {
        val service = new SamlRegisteredService();
        service.setId(1000);
        service.setServiceId("^https://sp.*");
        service.setMetadataLocation("jdbc://");

        val firstKey = new SamlRegisteredServiceCacheKey(service, new CriteriaSet(new EntityIdCriterion("https://sp1.example.org")));
        val secondKey = new SamlRegisteredServiceCacheKey(service, new CriteriaSet(new EntityIdCriterion("https://sp2.example.org")));
        assertNotEquals(firstKey, secondKey);
        assertEquals(firstKey, new SamlRegisteredServiceCacheKey(service, new CriteriaSet(new EntityIdCriterion("https://sp1.example.org"))));

        service.setMetadataLocation("classpath:sample-sp.xml");
        assertEquals(new SamlRegisteredServiceCacheKey(service, new CriteriaSet(new EntityIdCriterion("https://sp1.example.org"))),
            new SamlRegisteredServiceCacheKey(service, new CriteriaSet(new EntityIdCriterion("https://sp2.example.org"))));
    }

    @Test
    void verifyCacheKeyChangesWithMetadataSettings() {
        val criteriaSet = new CriteriaSet(new EntityIdCriterion("https://carmenwiki.osu.edu/shibboleth"));
        val service = new SamlRegisteredService();
        service.setId(1000);
        service.setServiceId(".+");
        service.setMetadataLocation("classpath:sample-sp.xml");
        val originalKey = new SamlRegisteredServiceCacheKey(service, criteriaSet);

        service.setDescription("Unrelated to metadata");
        assertEquals(originalKey, new SamlRegisteredServiceCacheKey(service, criteriaSet));

        service.setMetadataSignatureLocation("classpath:inc-md-pub.pem");
        val signedKey = new SamlRegisteredServiceCacheKey(service, criteriaSet);
        assertNotEquals(originalKey, signedKey);

        service.setMetadataCriteriaEntityAttributes(Map.of("http://macedir.org/entity-category", List.of("category")));
        assertNotEquals(signedKey, new SamlRegisteredServiceCacheKey(service, criteriaSet));
    }
}
