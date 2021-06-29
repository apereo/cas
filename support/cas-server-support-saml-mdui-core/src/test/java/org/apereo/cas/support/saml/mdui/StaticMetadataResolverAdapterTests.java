package org.apereo.cas.support.saml.mdui;

import org.apereo.cas.support.saml.AbstractOpenSamlTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StaticMetadataResolverAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAMLMetadata")
public class StaticMetadataResolverAdapterTests extends AbstractOpenSamlTests {
    @Test
    public void verifyOperation() {
        val resources = (Map) Map.of(new ClassPathResource("metadata.xml"), new MetadataFilterChain());
        val adapter = new StaticMetadataResolverAdapter(resources);
        adapter.setConfigBean(configBean);
        adapter.setMetadataResources(resources);
        adapter.buildMetadataResolverAggregate();
        assertNotNull(adapter.getEntityDescriptorForEntityId("https://carmenwiki.osu.edu/shibboleth"));
    }
}
