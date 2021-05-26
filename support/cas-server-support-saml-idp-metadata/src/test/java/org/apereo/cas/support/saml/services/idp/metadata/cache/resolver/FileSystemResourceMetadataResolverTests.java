package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.BeforeAll;
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
public class FileSystemResourceMetadataResolverTests extends BaseSamlIdPServicesTests {
    private static File METADATA_FILE;

    private static SamlIdPProperties PROPERTIES;

    @BeforeAll
    public static void setup() throws Exception {
        METADATA_FILE = File.createTempFile("sp-saml-metadata", ".xml");
        val content = IOUtils.toString(new ClassPathResource("sample-sp.xml").getInputStream(), StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(METADATA_FILE, content, StandardCharsets.UTF_8);

        PROPERTIES = new SamlIdPProperties();
        val path = new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath();
        PROPERTIES.getMetadata().getFileSystem().setLocation(path);
    }

    @Test
    public void verifyResolverSupports() throws Exception {
        val resolver = new FileSystemResourceMetadataResolver(PROPERTIES, openSamlConfigBean);
        val service = new SamlRegisteredService();
        service.setMetadataLocation(METADATA_FILE.getCanonicalPath());
        assertTrue(resolver.supports(service));
        assertFalse(resolver.isAvailable(null));
        assertTrue(resolver.resolve(null).isEmpty());
    }

    @Test
    public void verifyResolverWithBadSigningCert() throws Exception {
        val resolver = new FileSystemResourceMetadataResolver(PROPERTIES, openSamlConfigBean);
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
    public void verifyResolverWithDirectory() throws Exception {
        val resolver = new FileSystemResourceMetadataResolver(PROPERTIES, openSamlConfigBean);
        val service = new SamlRegisteredService();
        val file = new FileSystemResource("src/test/resources/md-dir").getFile().getCanonicalPath();
        service.setMetadataLocation(file);

        val resolvers = resolver.resolve(service);
        assertFalse(resolvers.isEmpty());
        val directoryResolver = resolvers.iterator().next();

        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion("https://idp.example.net/idp/shibboleth"));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        assertNotNull(directoryResolver.resolve(criteriaSet));
    }

    @Test
    public void verifyDefaultImpl() {
        val resolver = mock(SamlRegisteredServiceMetadataResolver.class);
        doCallRealMethod().when(resolver).saveOrUpdate(any());
        assertThrows(NotImplementedException.class, () -> resolver.saveOrUpdate(null));
    }
}
