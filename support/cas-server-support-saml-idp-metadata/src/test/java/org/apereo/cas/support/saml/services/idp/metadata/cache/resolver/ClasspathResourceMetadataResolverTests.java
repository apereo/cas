package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import module java.base;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.filter.EntityDescriptorCertificatesExpirationFilter;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.filter.FilterException;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterContext;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import java.security.cert.CertificateExpiredException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ClasspathResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAMLMetadata")
class ClasspathResourceMetadataResolverTests extends BaseSamlIdPServicesTests {

    @Test
    void verifyResolverSupports() throws Throwable {
        val resolver = getClasspathResourceMetadataResolver();
        val service = new SamlRegisteredService();
        service.setMetadataLocation("http://www.testshib.org/metadata/testshib-providers.xml");
        assertFalse(resolver.supports(service));
        service.setMetadataLocation("classpath:sample-sp.xml");
        assertTrue(resolver.supports(service));
    }

    @Test
    void verifyResolverResolves() throws Throwable {
        val resolver = getClasspathResourceMetadataResolver();
        val service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setId(1000);
        service.setMetadataLocation("classpath:sample-sp.xml");
        val results = resolver.resolve(service);
        assertFalse(results.isEmpty());
    }

    @Test
    void verifyMetadataCriteriaPattern() throws Throwable {
        val resolver = getClasspathResourceMetadataResolver();
        val service = new SamlRegisteredService();
        service.setName("Aggregate");
        service.setId(1000);
        service.setMetadataLocation("classpath:aggregate-md.xml");
        service.setMetadataCriteriaDirection("INCLUDE");
        service.setMetadataCriteriaPattern("https://gitlab\\..+");
        service.setMetadataCriteriaRoles(StringUtils.EMPTY);
        val metadataResolver = resolver.resolve(service).iterator().next();
        assertNotNull(metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://gitlab.com"))));
        assertNull(metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://issues.shibboleth.net/shibboleth"))));

        service.setMetadataCriteriaPattern("https://GITLAB\\..+");
        val caseSensitiveResolver = resolver.resolve(service).iterator().next();
        assertNull(caseSensitiveResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://gitlab.com"))));
    }

    @Test
    void verifyExpiredCertificateFailsFilterWithCause() throws Throwable {
        val metadata = new ClassPathResource("sample-sp.xml").getContentAsString(StandardCharsets.UTF_8);
        val entityDescriptor = SamlUtils.transformSamlObject(openSamlConfigBean, metadata, EntityDescriptor.class);
        assertNotNull(entityDescriptor);
        val exception = assertThrows(FilterException.class,
            () -> new EntityDescriptorCertificatesExpirationFilter().filter(entityDescriptor, new MetadataFilterContext()));
        assertInstanceOf(CertificateExpiredException.class, exception.getCause());
    }

    @Test
    void verifyExpiredCertificateEntitiesRemovedFromAggregate() throws Throwable {
        val metadata = new ClassPathResource("aggregate-expired-cert.xml").getContentAsString(StandardCharsets.UTF_8);
        val aggregate = SamlUtils.transformSamlObject(openSamlConfigBean, metadata, EntitiesDescriptor.class);
        assertNotNull(aggregate);
        val filtered = assertInstanceOf(EntitiesDescriptor.class,
            new EntityDescriptorCertificatesExpirationFilter().filter(aggregate, new MetadataFilterContext()));
        assertEquals(List.of("https://valid.example.org"),
            filtered.getEntityDescriptors().stream().map(EntityDescriptor::getEntityID).toList());
        assertEquals(1, filtered.getEntitiesDescriptors().size());
        assertEquals(List.of("https://nested-valid.example.org"),
            filtered.getEntitiesDescriptors().getFirst().getEntityDescriptors().stream().map(EntityDescriptor::getEntityID).toList());
    }

    @Test
    void verifyResolverRefusesExpiredCertificateEntitiesOfAggregate() throws Throwable {
        val resolver = getClasspathResourceMetadataResolver();
        val service = new SamlRegisteredService();
        service.setName("AggregateExpiredCertificates");
        service.setId(1000);
        service.setMetadataLocation("classpath:aggregate-expired-cert.xml");
        service.setMetadataCriteriaRoles(StringUtils.EMPTY);
        service.setValidateMetadataCertificates(true);
        val metadataResolver = resolver.resolve(service).iterator().next();
        assertNotNull(metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://valid.example.org"))));
        assertNotNull(metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://nested-valid.example.org"))));
        assertNull(metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://expired.example.org"))));
        assertNull(metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://nested-expired.example.org"))));

        service.setValidateMetadataCertificates(false);
        val uncheckedResolver = resolver.resolve(service).iterator().next();
        assertNotNull(uncheckedResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://expired.example.org"))));
    }

    @Test
    void verifyResolverFails() throws Throwable {
        val resolver = getClasspathResourceMetadataResolver();
        val service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setId(1000);
        service.setMetadataLocation("classpath:unknown-123456.xml");
        val results = resolver.resolve(service);
        assertTrue(results.isEmpty());
    }

    private ClasspathResourceMetadataResolver getClasspathResourceMetadataResolver() throws IOException {
        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());
        return new ClasspathResourceMetadataResolver(props, openSamlConfigBean);
    }
}
