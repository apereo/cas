package org.apereo.cas.support.saml.mdui;

import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamicMetadataResolverAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAMLMetadata")
public class DynamicMetadataResolverAdapterTests extends AbstractOpenSamlTests {
    @Test
    public void verifyOperation() throws Exception {
        val resource = new UrlResource(new URI("http://localhost:6622/entities/"));
        val adapter = new DynamicMetadataResolverAdapter(Map.of(resource, new MetadataFilterChain()));
        adapter.setConfigBean(configBean);

        val entity = IOUtils.toString(new ClassPathResource("metadata.xml").getInputStream(), StandardCharsets.UTF_8);
        try (val webServer = new MockWebServer(6622,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            assertNotNull(adapter.getEntityDescriptorForEntityId("https://carmenwiki.osu.edu/shibboleth"));
        }
    }
}
