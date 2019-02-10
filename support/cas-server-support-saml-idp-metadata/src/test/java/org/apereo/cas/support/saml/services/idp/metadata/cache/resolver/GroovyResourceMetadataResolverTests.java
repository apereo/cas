package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class GroovyResourceMetadataResolverTests extends BaseSamlIdPServicesTests {


    @Test
    public void verifyResolverSupports() {
        val props = new SamlIdPProperties();
        props.getMetadata().setLocation(new FileSystemResource(FileUtils.getTempDirectory()));
        val resolver = new GroovyResourceMetadataResolver(props, openSamlConfigBean);
        val service = new SamlRegisteredService();
        service.setMetadataLocation("classpath:GroovyMetadataResolver.groovy");
        assertTrue(resolver.supports(service));
    }

    @Test
    public void verifyResolverResolves() {
        val props = new SamlIdPProperties();
        props.getMetadata().setLocation(new FileSystemResource(FileUtils.getTempDirectory()));
        val resolver = new GroovyResourceMetadataResolver(props, openSamlConfigBean);
        val service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setId(1000);
        service.setMetadataLocation("classpath:GroovyMetadataResolver.groovy");
        val results = resolver.resolve(service);
        assertFalse(results.isEmpty());
    }
}
