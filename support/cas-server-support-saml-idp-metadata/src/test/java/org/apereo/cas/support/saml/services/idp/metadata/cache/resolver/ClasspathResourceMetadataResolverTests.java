package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ClasspathResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAMLMetadata")
public class ClasspathResourceMetadataResolverTests extends BaseSamlIdPServicesTests {

    @Test
    public void verifyResolverSupports() throws Exception {
        val resolver = getClasspathResourceMetadataResolver();
        val service = new SamlRegisteredService();
        service.setMetadataLocation("http://www.testshib.org/metadata/testshib-providers.xml");
        assertFalse(resolver.supports(service));
        service.setMetadataLocation("classpath:sample-sp.xml");
        assertTrue(resolver.supports(service));
    }

    @Test
    public void verifyResolverResolves() throws Exception {
        val resolver = getClasspathResourceMetadataResolver();
        val service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setId(1000);
        service.setMetadataLocation("classpath:sample-sp.xml");
        val results = resolver.resolve(service);
        assertFalse(results.isEmpty());
    }

    @Test
    public void verifyResolverFails() throws Exception {
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
