package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("SAML")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.location=file:/tmp")
public class JsonResourceMetadataResolverTests extends BaseSamlIdPServicesTests {

    @Test
    public void verifyResolverResolves() throws Exception {
        val props = new SamlIdPProperties();
        val dir = new FileSystemResource(FileUtils.getTempDirectory());
        props.getMetadata().setLocation(dir.getFile().getCanonicalPath());
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
    }

    /**
     * Make sure default file:/etc/cas/saml URI syntax is parsed correctly.
     */
    @Test
    public void verifyResolverResolvesWithFileUri() throws Exception {
        val props = new SamlIdPProperties();
        props.getMetadata().setLocation("file:/etc/cas/saml");
        val resolver = new JsonResourceMetadataResolver(props, openSamlConfigBean);
        assertNotNull(resolver);
    }
}
