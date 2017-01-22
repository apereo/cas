package org.apereo.cas.support.saml.mdui;

import org.apache.commons.io.input.ClosedInputStream;
import org.apereo.cas.util.EncodingUtils;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
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
        final String encodedId = EncodingUtils.urlEncode(entityId);

        if (resource instanceof UrlResource) {
            final URL url = new URL(resource.getURL().toExternalForm().concat(encodedId));
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
