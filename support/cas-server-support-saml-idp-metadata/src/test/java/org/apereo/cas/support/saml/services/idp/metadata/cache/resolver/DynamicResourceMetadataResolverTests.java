package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamicResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAML")
public class DynamicResourceMetadataResolverTests extends BaseSamlIdPServicesTests {

    @Test
    public void verifyResolverSupports() throws Exception {
        val props = new SamlIdPProperties();
        props.getMetadata().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());
        val resolver = new MetadataQueryProtocolMetadataResolver(props, openSamlConfigBean);
        val service = new SamlRegisteredService();
        service.setMetadataLocation("http://www.testshib.org/metadata/testshib-providers.xml");
        assertFalse(resolver.supports(service));
        service.setMetadataLocation("http://mdq-preview.incommon.org/entities/{0}");
        assertTrue(resolver.supports(service));
    }

    @Test
    public void verifyResolverResolves() throws Exception {
        val props = new SamlIdPProperties();
        props.getMetadata().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());
        val resolver = new MetadataQueryProtocolMetadataResolver(props, openSamlConfigBean);
        val service = new SamlRegisteredService();
        service.setId(100);
        service.setName("Dynamic");
        service.setMetadataLocation("http://mdq-preview.incommon.org/entities/{0}");
        service.setServiceId("https://webauth.cmc.edu/idp/shibboleth");
        val results = resolver.resolve(service);
        assertFalse(results.isEmpty());
    }
}
