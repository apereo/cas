package org.apereo.cas.support.saml;

import module java.base;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.springframework.core.io.Resource;

/**
 * This is {@link InMemoryResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InMemoryResourceMetadataResolver extends DOMMetadataResolver {

    public InMemoryResourceMetadataResolver(final String metadataResource, final OpenSamlConfigBean configBean) {
        this(new ByteArrayInputStream(metadataResource.getBytes(StandardCharsets.UTF_8)), configBean);
    }

    public InMemoryResourceMetadataResolver(final Resource metadataResource, final OpenSamlConfigBean configBean) throws IOException {
        this(metadataResource.getInputStream(), configBean);
    }

    public InMemoryResourceMetadataResolver(final InputStream metadataResource, final OpenSamlConfigBean configBean) {
        super(SamlUtils.getRootElementFrom(metadataResource, configBean));
        setParserPool(configBean.getParserPool());
        setResolveViaPredicatesOnly(true);
    }

    public InMemoryResourceMetadataResolver(final File metadataResource, final OpenSamlConfigBean configBean) throws IOException {
        this(Files.newInputStream(metadataResource.toPath()), configBean);
        setParserPool(configBean.getParserPool());
        setResolveViaPredicatesOnly(true);
    }
}
