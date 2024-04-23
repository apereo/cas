package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

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
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.core.io.FileSystemResource;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;

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
}
