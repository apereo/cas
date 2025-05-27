package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import com.google.common.collect.Iterables;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link FileSystemResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAMLMetadata")
class FileSystemResourceMetadataResolverTests extends BaseSamlIdPServicesTests {
    private static File METADATA_FILE;

    private SamlRegisteredServiceMetadataResolver metadataResolver;

    @BeforeAll
    public static void setup() throws Exception {
        METADATA_FILE = File.createTempFile("sp-saml-metadata", ".xml");
        val content = IOUtils.toString(new ClassPathResource("sample-sp.xml").getInputStream(), StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(METADATA_FILE, content, StandardCharsets.UTF_8);
    }

    @BeforeEach
    void beforeEach() throws Exception {
        val properties = new SamlIdPProperties();
        val path = new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath();
        properties.getMetadata().getFileSystem().setLocation(path);
        this.metadataResolver = new FileSystemResourceMetadataResolver(properties, openSamlConfigBean);
    }

    @Test
    void verifyResolverSupports() throws Throwable {
        val service = new SamlRegisteredService();
        service.setMetadataLocation(METADATA_FILE.getCanonicalPath());
        assertTrue(metadataResolver.supports(service));
        assertFalse(metadataResolver.isAvailable(null));
        assertTrue(metadataResolver.resolve(null).isEmpty());
    }

    @Test
    void verifyResolverWithBadSigningCert() throws Throwable {
        val service = new SamlRegisteredService();
        service.setMetadataMaxValidity(30000);
        service.setMetadataCriteriaRoles(String.join(",", Set.of(
            IDPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart(),
            SPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart())));
        service.setMetadataLocation(METADATA_FILE.getCanonicalPath());
        service.setMetadataSignatureLocation("classpath:inc-md-cert.pem");
        assertTrue(metadataResolver.resolve(service).isEmpty());
    }

    @Test
    void verifyResolverWithEntityAttributes() throws Throwable {
        val service = new SamlRegisteredService();
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setMetadataCriteriaEntityAttributes(Map.of("http://macedir.org/entity-category",
            List.of("http://id.incommon.org/category/research-and-scholarship", "http://refeds.org/category/research-and-scholarship")));
        service.setMetadataLocation(METADATA_FILE.getCanonicalPath());
        service.setMetadataCriteriaDirection("include");
        val resolvers = metadataResolver.resolve(service);
        assertFalse(resolvers.isEmpty());
        val resolver = resolvers.iterator().next();
        val criteria = getCriteriaFor(service.getServiceId());
        assertNotNull(resolver.resolve(criteria));
    }


    @Test
    void verifyResolverWithExpiredMetadataCertificates() throws Throwable {
        val service = new SamlRegisteredService();
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setMetadataLocation(METADATA_FILE.getCanonicalPath());
        service.setValidateMetadataCertificates(true);
        val resolvers = metadataResolver.resolve(service);
        assertTrue(resolvers.isEmpty());
    }

    @Test
    void verifyResolverWithDirectory() throws Throwable {
        val service = new SamlRegisteredService();
        val file = new FileSystemResource("src/test/resources/md-dir").getFile().getCanonicalPath();
        service.setMetadataLocation(file);

        val resolvers = metadataResolver.resolve(service);
        assertFalse(resolvers.isEmpty());
        val directoryResolver = resolvers.iterator().next();

        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion("sp1:example"));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        assertEquals(1, Iterables.size(directoryResolver.resolve(criteriaSet)));
    }

    @Test
    void verifyDefaultImpl() {
        val mock = mock(SamlRegisteredServiceMetadataResolver.class);
        doCallRealMethod().when(mock).saveOrUpdate(any());
        assertThrows(NotImplementedException.class, () -> mock.saveOrUpdate(null));
    }
}
