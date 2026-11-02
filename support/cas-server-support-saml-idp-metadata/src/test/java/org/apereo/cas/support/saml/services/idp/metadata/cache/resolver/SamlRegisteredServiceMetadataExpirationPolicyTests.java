package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import module java.base;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.CachedMetadataResolverResult;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCacheKey;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceMetadataExpirationPolicy;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.core.io.FileSystemResource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlRegisteredServiceMetadataExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAMLMetadata")
class SamlRegisteredServiceMetadataExpirationPolicyTests extends BaseSamlIdPServicesTests {
    @Test
    void verifyPolicyByEntityCache() throws Throwable {
        val policy = new SamlRegisteredServiceMetadataExpirationPolicy(Beans.newDuration("PT5M"));

        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());

        val service = new SamlRegisteredService();
        service.setMetadataExpirationDuration(StringUtils.EMPTY);
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setMetadataLocation("classpath:GroovyMetadataResolver.groovy");
        val cacheKey = new SamlRegisteredServiceCacheKey(service, new CriteriaSet());

        val resolver = mock(MetadataResolver.class);
        val entity = mock(EntityDescriptor.class);
        val entityCacheDuration = Duration.ofSeconds(10);
        when(entity.getCacheDuration()).thenReturn(entityCacheDuration);
        when(resolver.resolveSingle(any())).thenReturn(entity);

        val result = CachedMetadataResolverResult.builder().metadataResolver(resolver).build();
        assertEquals(entityCacheDuration.toNanos(), policy.expireAfterCreate(cacheKey, result, System.currentTimeMillis()));

        when(resolver.resolveSingle(any())).thenThrow(new IllegalArgumentException());
        assertEquals(policy.defaultExpiration().toNanos(), policy.expireAfterCreate(cacheKey, result, System.currentTimeMillis()));
        assertEquals(policy.defaultExpiration().toNanos(),
            policy.expireAfterUpdate(cacheKey, result, 1000, policy.defaultExpiration().toNanos()));
    }

    @Test
    void verifyPolicyBySpEntityCache() throws Throwable {
        val policy = new SamlRegisteredServiceMetadataExpirationPolicy(Beans.newDuration("PT5M"));
        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());

        val service = new SamlRegisteredService();
        service.setMetadataExpirationDuration(StringUtils.EMPTY);
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setMetadataLocation("classpath:GroovyMetadataResolver.groovy");
        val cacheKey = new SamlRegisteredServiceCacheKey(service, new CriteriaSet());
        val resolver = mock(MetadataResolver.class);

        val entity = mock(EntityDescriptor.class);
        val spCacheDuration = Duration.ofSeconds(30);
        when(entity.getCacheDuration()).thenReturn(spCacheDuration);
        when(resolver.resolveSingle(argThat(argument -> argument != null && argument.size() == 1))).thenReturn(entity);
        when(resolver.resolveSingle(argThat(argument -> argument != null && argument.size() > 1))).thenReturn(null);

        val result = CachedMetadataResolverResult.builder().metadataResolver(resolver).build();
        assertEquals(spCacheDuration.toNanos(), policy.expireAfterCreate(cacheKey, result, System.currentTimeMillis()));
    }

    @Test
    @SuppressWarnings("JavaTimeDefaultTimeZone")
    void verifyPolicyByServiceExpirationPolicy() throws Throwable {
        val policy = new SamlRegisteredServiceMetadataExpirationPolicy(Beans.newDuration("PT5M"));
        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());

        val service = new SamlRegisteredService();
        service.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy()
            .setExpirationDate(LocalDate.now(Clock.systemDefaultZone()).plusDays(1).toString()));

        service.setMetadataExpirationDuration(StringUtils.EMPTY);
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setMetadataLocation("classpath:GroovyMetadataResolver.groovy");
        val cacheKey = new SamlRegisteredServiceCacheKey(service, new CriteriaSet());
        val resolver = mock(MetadataResolver.class);

        val entity = mock(EntityDescriptor.class);
        when(entity.getCacheDuration()).thenReturn(null);
        when(resolver.resolveSingle(argThat(argument -> argument != null && argument.size() == 1))).thenReturn(entity);
        when(resolver.resolveSingle(argThat(argument -> argument != null && argument.size() > 1))).thenReturn(null);

        val result = CachedMetadataResolverResult.builder().metadataResolver(resolver).build();
        assertNotEquals(policy.defaultExpiration().toNanos(), policy.expireAfterCreate(cacheKey, result, System.currentTimeMillis()));
    }

    @Test
    void verifyMetadataCacheDurationIsBounded() throws Throwable {
        val policy = new SamlRegisteredServiceMetadataExpirationPolicy(Beans.newDuration("PT5M"),
            Beans.newDuration("PT1M"), Beans.newDuration("PT1H"));
        val service = new SamlRegisteredService();
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        val cacheKey = new SamlRegisteredServiceCacheKey(service, new CriteriaSet());
        val resolver = mock(MetadataResolver.class);
        val entity = mock(EntityDescriptor.class);
        when(resolver.resolveSingle(any())).thenReturn(entity);
        val result = CachedMetadataResolverResult.builder().metadataResolver(resolver).build();

        when(entity.getCacheDuration()).thenReturn(Duration.ZERO);
        assertEquals(Duration.ofMinutes(1).toNanos(), policy.expireAfterCreate(cacheKey, result, System.currentTimeMillis()));
        when(entity.getCacheDuration()).thenReturn(Duration.ofDays(2));
        assertEquals(Duration.ofHours(1).toNanos(), policy.expireAfterCreate(cacheKey, result, System.currentTimeMillis()));
        when(entity.getCacheDuration()).thenReturn(Duration.ofMinutes(10));
        assertEquals(Duration.ofMinutes(10).toNanos(), policy.expireAfterCreate(cacheKey, result, System.currentTimeMillis()));
    }

    @Test
    void verifyLifetimeEndsWithValidity() throws Throwable {
        val policy = new SamlRegisteredServiceMetadataExpirationPolicy(Beans.newDuration("PT5M"));
        val service = new SamlRegisteredService();
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setMetadataExpirationDuration("PT1H");
        val cacheKey = new SamlRegisteredServiceCacheKey(service, new CriteriaSet());
        val resolver = mock(MetadataResolver.class);
        val entity = mock(EntityDescriptor.class);
        when(resolver.resolveSingle(any())).thenReturn(entity);
        when(entity.getCacheDuration()).thenReturn(null);
        val result = CachedMetadataResolverResult.builder().metadataResolver(resolver).build();

        when(entity.getValidUntil()).thenReturn(Instant.now(Clock.systemUTC()).plus(Duration.ofMinutes(10)));
        val lifetime = Duration.ofNanos(policy.expireAfterCreate(cacheKey, result, System.currentTimeMillis()));
        assertTrue(lifetime.compareTo(Duration.ofMinutes(10)) <= 0 && lifetime.compareTo(Duration.ofMinutes(9)) > 0);

        val aggregate = mock(EntitiesDescriptor.class);
        when(aggregate.getValidUntil()).thenReturn(Instant.now(Clock.systemUTC()).plus(Duration.ofMinutes(2)));
        when(entity.getParent()).thenReturn(aggregate);
        assertTrue(Duration.ofNanos(policy.expireAfterCreate(cacheKey, result, System.currentTimeMillis())).compareTo(Duration.ofMinutes(2)) <= 0);

        when(aggregate.getValidUntil()).thenReturn(Instant.now(Clock.systemUTC()).minus(Duration.ofMinutes(2)));
        when(entity.getValidUntil()).thenReturn(Instant.now(Clock.systemUTC()).minus(Duration.ofMinutes(1)));
        assertEquals(Duration.ofHours(1).toNanos(), policy.expireAfterCreate(cacheKey, result, System.currentTimeMillis()),
            "Metadata that is no longer valid keeps its lifetime instead of being reloaded on every request");
    }
}
