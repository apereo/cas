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
        resolver.destroy();
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
