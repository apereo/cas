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

    private SamlRegisteredServiceMetadataResolver resolver;

    @BeforeAll
    public static void setup() throws Exception {
        METADATA_FILE = File.createTempFile("sp-saml-metadata", ".xml");
        val content = IOUtils.toString(new ClassPathResource("sample-sp.xml").getInputStream(), StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(METADATA_FILE, content, StandardCharsets.UTF_8);
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        val properties = new SamlIdPProperties();
        val path = new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath();
        properties.getMetadata().getFileSystem().setLocation(path);
        this.resolver = new FileSystemResourceMetadataResolver(properties, openSamlConfigBean);
    }
    
    @Test
    void verifyResolverSupports() throws Exception {
        val service = new SamlRegisteredService();
        service.setMetadataLocation(METADATA_FILE.getCanonicalPath());
        assertTrue(resolver.supports(service));
        assertFalse(resolver.isAvailable(null));
        assertTrue(resolver.resolve(null).isEmpty());
    }

    @Test
    void verifyResolverWithBadSigningCert() throws Exception {
        val service = new SamlRegisteredService();
        service.setMetadataMaxValidity(30000);
        service.setMetadataCriteriaRoles(String.join(",", Set.of(
            IDPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart(),
            SPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart())));
        service.setMetadataLocation(METADATA_FILE.getCanonicalPath());
        service.setMetadataSignatureLocation("classpath:inc-md-cert.pem");
        assertTrue(resolver.resolve(service).isEmpty());
    }

    @Test
    void verifyResolverWithDirectory() throws Exception {
        val service = new SamlRegisteredService();
        val file = new FileSystemResource("src/test/resources/md-dir").getFile().getCanonicalPath();
        service.setMetadataLocation(file);

        val resolvers = resolver.resolve(service);
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
