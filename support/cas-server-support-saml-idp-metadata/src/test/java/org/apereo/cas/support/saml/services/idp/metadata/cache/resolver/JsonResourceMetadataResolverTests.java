package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import module java.base;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("SAMLMetadata")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=file:/tmp")
class JsonResourceMetadataResolverTests extends BaseSamlIdPServicesTests {

    @Test
    void verifyResolverResolves() throws Throwable {
        val props = new SamlIdPProperties();
        val dir = new FileSystemResource(FileUtils.getTempDirectory());
        props.getMetadata().getFileSystem().setLocation(dir.getFile().getCanonicalPath());
        FileUtils.copyFile(new ClassPathResource("saml-sp-metadata.json").getFile(),
            new File(FileUtils.getTempDirectory(), "saml-sp-metadata.json"));
        val service = new SamlRegisteredService();
        val resolver = new JsonResourceMetadataResolver(props, openSamlConfigBean);
        service.setName("Example");
        service.setId(1000);
        service.setServiceId("https://example.org/saml");
        service.setMetadataLocation("json://");
        assertTrue(resolver.isAvailable(service));
        assertTrue(resolver.supports(service));
        val results = resolver.resolve(service);
        assertFalse(results.isEmpty());
        val metadataResolver = results.iterator().next();
        val resolved = metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://example.org/saml")));
        assertNotNull(resolved);
        assertEquals(List.of(new File(dir.getFile().getCanonicalPath(), "saml-sp-metadata.json")), resolver.getMetadataSourceFiles(service));
    }

    @Test
    void verifyResolverReadsFileWhenItChanges() throws Throwable {
        val dir = Files.createTempDirectory("saml-sp-metadata").toFile();
        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(dir.getCanonicalPath());
        val resolver = new JsonResourceMetadataResolver(props, openSamlConfigBean);
        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId("https://example.org/saml");
        service.setMetadataLocation("json://");
        assertFalse(resolver.isAvailable(service));
        assertTrue(resolver.resolve(service).isEmpty());

        val jsonFile = new File(dir, "saml-sp-metadata.json");
        FileUtils.copyFile(new ClassPathResource("saml-sp-metadata.json").getFile(), jsonFile);
        assertTrue(resolver.isAvailable(service));
        assertFalse(resolver.resolve(service).isEmpty());

        val json = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8).replace("https://example.org/saml", "https://example.org/other");
        FileUtils.writeStringToFile(jsonFile, json, StandardCharsets.UTF_8);
        assertTrue(jsonFile.setLastModified(jsonFile.lastModified() + 5000));
        assertTrue(resolver.resolve(service).isEmpty());
        service.setServiceId("https://example.org/other");
        assertFalse(resolver.resolve(service).isEmpty());
    }

    /**
     * Make sure default file:/etc/cas/saml URI syntax is parsed correctly.
     */
    @Test
    void verifyResolverResolvesWithFileUri() {
        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation("file:/etc/cas/saml");
        val resolver = new JsonResourceMetadataResolver(props, openSamlConfigBean);
        assertNotNull(resolver);
    }
}
