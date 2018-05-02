package org.apereo.cas.support.saml;

import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * This is {@link InMemoryResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InMemoryResourceMetadataResolver extends DOMMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryResourceMetadataResolver.class);

    public InMemoryResourceMetadataResolver(final Resource metadataResource, final OpenSamlConfigBean configBean) throws Exception {
        super(getMetadataRootElement(metadataResource.getInputStream(), configBean));
    }

    public InMemoryResourceMetadataResolver(final InputStream metadataResource, final OpenSamlConfigBean configBean) throws Exception {
        super(getMetadataRootElement(metadataResource, configBean));
    }

    public InMemoryResourceMetadataResolver(final File metadataResource, final OpenSamlConfigBean configBean) throws Exception {
        super(getMetadataRootElement(Files.newInputStream(metadataResource.toPath()), configBean));
    }

    private static Element getMetadataRootElement(final InputStream metadataResource,
                                                  final OpenSamlConfigBean configBean) throws Exception {
        final Document document = configBean.getParserPool().parse(metadataResource);
        return document.getDocumentElement();
    }
}
