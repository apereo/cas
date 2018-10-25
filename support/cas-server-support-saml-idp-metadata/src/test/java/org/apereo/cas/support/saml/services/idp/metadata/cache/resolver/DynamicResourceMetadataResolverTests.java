package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.category.FileSystemCategory;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamicResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */

@Category(FileSystemCategory.class)
public class DynamicResourceMetadataResolverTests extends BaseSamlIdPServicesTests {

    @Test
    public void verifyResolverSupports() {
        val props = new SamlIdPProperties();
        props.getMetadata().setLocation(new FileSystemResource(FileUtils.getTempDirectory()));
        val resolver = new MetadataQueryProtocolMetadataResolver(props, openSamlConfigBean);
        val service = new SamlRegisteredService();
        service.setMetadataLocation("http://www.testshib.org/metadata/testshib-providers.xml");
        assertFalse(resolver.supports(service));
        service.setMetadataLocation("http://mdq-beta.incommon.org/global/entities/{0}");
        assertTrue(resolver.supports(service));
    }

    @Test
    public void verifyResolverResolves() {
        val props = new SamlIdPProperties();
        props.getMetadata().setLocation(new FileSystemResource(FileUtils.getTempDirectory()));
        val resolver = new MetadataQueryProtocolMetadataResolver(props, openSamlConfigBean);
        val service = new SamlRegisteredService();
        service.setId(100);
        service.setName("Dynamic");
        service.setMetadataLocation("http://mdq-beta.incommon.org/global/entities/{0}");
        service.setServiceId("https://webauth.cmc.edu/idp/shibboleth");
        val results = resolver.resolve(service);
        assertFalse(results.isEmpty());
    }
}
