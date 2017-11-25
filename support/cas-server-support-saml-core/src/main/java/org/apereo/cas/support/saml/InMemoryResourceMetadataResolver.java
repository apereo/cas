package org.apereo.cas.support.saml;

import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.resource.Resource;
import org.opensaml.saml.metadata.resolver.impl.ResourceBackedMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This is {@link InMemoryResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InMemoryResourceMetadataResolver extends ResourceBackedMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryResourceMetadataResolver.class);
    private final Resource metadataResource;

    public InMemoryResourceMetadataResolver(final Resource resource) throws IOException {
        super(resource);
        this.metadataResource = resource;
    }

    @Override
    protected byte[] fetchMetadata() throws ResolverException {
        try {
            return inputstreamToByteArray(metadataResource.getInputStream());
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ResolverException(e.getMessage(), e);
        }
    }
}
