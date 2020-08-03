package org.apereo.cas.support.saml.mdui;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ClosedInputStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A metadata adapter {@link DynamicMetadataResolverAdapter}
 * that queries a metadata server on demand following
 * the metadata query protocol.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@NoArgsConstructor
public class DynamicMetadataResolverAdapter extends AbstractMetadataResolverAdapter {

    /**
     * Instantiates a new static metadata resolver adapter.
     *
     * @param metadataResources the metadata resources
     */
    public DynamicMetadataResolverAdapter(final Map<Resource, MetadataFilterChain> metadataResources) {
        super(metadataResources);
    }

    @Override
    public EntityDescriptor getEntityDescriptorForEntityId(final String entityId) {
        buildMetadataResolverAggregate(entityId);
        return super.getEntityDescriptorForEntityId(entityId);
    }

    @Override
    protected InputStream getResourceInputStream(final Resource resource, final String entityId) throws IOException {
        if (resource instanceof UrlResource && resource.getURL().toExternalForm().toLowerCase().endsWith("/entities/")) {
            HttpResponse response = null;
            try {
                val encodedId = EncodingUtils.urlEncode(entityId);
                val url = resource.getURL().toExternalForm().concat(encodedId);
                LOGGER.debug("Locating metadata input stream for [{}] via [{}]", encodedId, url);
                response = HttpUtils.executeGet(url, Map.of(), Map.of("Accept", "*/*"));
                if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    return new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
                }
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            } finally {
                HttpUtils.close(response);
            }
        }
        return ClosedInputStream.CLOSED_INPUT_STREAM;
    }
}
