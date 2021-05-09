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
 * This is {@link GroovyResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Groovy")
public class GroovyResourceMetadataResolverTests extends BaseSamlIdPServicesTests {
    @Test
    public void verifyResolverSupports() throws Exception {
        val resolver = getGroovyResourceMetadataResolver();
        val service = new SamlRegisteredService();
        service.setMetadataLocation("classpath:GroovyMetadataResolver.groovy");
        assertTrue(resolver.supports(service));
        assertTrue(resolver.isAvailable(service));
    }

    @Test
    public void verifyResolverDoesNotSupport() throws Exception {
        val resolver = getGroovyResourceMetadataResolver();
        val service = new SamlRegisteredService();
        service.setMetadataLocation("file:UnknownFile.xyz");
        assertFalse(resolver.isAvailable(service));
    }

    @Test
    public void verifyResolverMissingResource() throws Exception {
        val resolver = getGroovyResourceMetadataResolver();
        val service = new SamlRegisteredService();
        service.setMetadataLocation("file:/doesnotexist/UnknownScript.groovy");
        val results = resolver.resolve(service);
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyResolverResolves() throws Exception {
        val resolver = getGroovyResourceMetadataResolver();
        val service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setId(1000);
        service.setMetadataLocation("classpath:GroovyMetadataResolver.groovy");
        val results = resolver.resolve(service);
        assertFalse(results.isEmpty());
    }

    private GroovyResourceMetadataResolver getGroovyResourceMetadataResolver() throws IOException {
        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());
        return new GroovyResourceMetadataResolver(props, openSamlConfigBean);
    }
}
