package org.apereo.cas.jmx.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepositoryCache;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.DefaultPrincipalAttributesRepositoryCache;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalAttributesCacheManagedResourceTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("JMX")
@ExtendWith(CasTestExtension.class)
class PrincipalAttributesCacheManagedResourceTests {
    @Test
    void verifyCacheInvalidation() throws Throwable {
        try (val cache = new DefaultPrincipalAttributesRepositoryCache()) {
            val beans = new StaticListableBeanFactory(Map.of("cache", cache));
            val resource = new PrincipalAttributesCacheManagedResource(beans.getBeanProvider(PrincipalAttributesRepositoryCache.class));
            val repository = new CachingPrincipalAttributesRepository(TimeUnit.HOURS.name(), 1);
            val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("casuser");
            val firstService = new CasRegisteredService();
            firstService.setId(1);
            val secondService = new CasRegisteredService();
            secondService.setId(2);
            val attributes = Map.<String, List<Object>>of("mail", List.of("casuser@example.org"));
            cache.putAttributes(firstService, repository, principal.getId(), attributes);
            cache.putAttributes(secondService, repository, principal.getId(), attributes);

            assertTrue(resource.isAvailable());
            assertEquals(attributes, cache.fetchAttributes(firstService, repository, principal));
            assertEquals(attributes, cache.fetchAttributes(secondService, repository, principal));
            resource.invalidate();
            assertTrue(cache.fetchAttributes(firstService, repository, principal).isEmpty());
            assertTrue(cache.fetchAttributes(secondService, repository, principal).isEmpty());
        }
    }

    @Test
    void verifyCacheUnavailable() {
        val beans = new StaticListableBeanFactory();
        val resource = new PrincipalAttributesCacheManagedResource(beans.getBeanProvider(PrincipalAttributesRepositoryCache.class));

        assertFalse(resource.isAvailable());
        assertThrows(IllegalStateException.class, resource::invalidate);
    }
}
