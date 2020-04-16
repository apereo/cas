package org.apereo.cas.support.saml;

import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * This is {@link InMemoryResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InMemoryResourceMetadataResolver extends DOMMetadataResolver {

    public InMemoryResourceMetadataResolver(final Resource metadataResource, final OpenSamlConfigBean configBean) throws IOException {
        super(SamlUtils.getRootElementFrom(metadataResource.getInputStream(), configBean));
        setParserPool(configBean.getParserPool());
    }

    public InMemoryResourceMetadataResolver(final InputStream metadataResource, final OpenSamlConfigBean configBean) {
        super(SamlUtils.getRootElementFrom(metadataResource, configBean));
        setParserPool(configBean.getParserPool());
    }

    public InMemoryResourceMetadataResolver(final File metadataResource, final OpenSamlConfigBean configBean) throws IOException {
        super(SamlUtils.getRootElementFrom(Files.newInputStream(metadataResource.toPath()), configBean));
        setParserPool(configBean.getParserPool());
    }
}
