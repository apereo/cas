package org.apereo.cas.support.saml.mdui;

import org.apache.commons.io.input.ClosedInputStream;
import org.apereo.cas.util.EncodingUtils;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * A metadata adapter {@link DynamicMetadataResolverAdapter}
 * that queries a metadata server on demand following
 * the metadata query protocol.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class DynamicMetadataResolverAdapter extends AbstractMetadataResolverAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicMetadataResolverAdapter.class);

    /**
     * Instantiates a new static metadata resolver adapter.
     *
     * @param metadataResources the metadata resources
     */
    public DynamicMetadataResolverAdapter(final Map<Resource, MetadataFilterChain> metadataResources) {
        super(metadataResources);
    }

    public DynamicMetadataResolverAdapter() {
    }

    @Override
    public EntityDescriptor getEntityDescriptorForEntityId(final String entityId) {
        buildMetadataResolverAggregate(entityId);
        return super.getEntityDescriptorForEntityId(entityId);
    }

    @Override
    protected InputStream getResourceInputStream(final Resource resource, final String entityId) throws IOException {
        if (resource instanceof UrlResource && resource.getURL().toExternalForm().toLowerCase().endsWith("/entities/")) {
            final String encodedId = EncodingUtils.urlEncode(entityId);
            final URL url = new URL(resource.getURL().toExternalForm().concat(encodedId));

            LOGGER.debug("Locating metadata input stream for [{}] via [{}]", encodedId, url);
            final HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
            httpcon.setDoOutput(true);
            httpcon.addRequestProperty("Accept", "*/*");
            httpcon.setRequestMethod("GET");
            httpcon.connect();
            return httpcon.getInputStream();
        }
        return ClosedInputStream.CLOSED_INPUT_STREAM;
    }
}
