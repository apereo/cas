package org.apereo.cas.support.saml.mdui;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ClosedInputStream;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpMethod;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
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
        if (resource instanceof UrlResource && resource.getURL().toExternalForm().toLowerCase(Locale.ENGLISH).endsWith("/entities/")) {
            HttpResponse response = null;
            try {
                val encodedId = EncodingUtils.urlEncode(entityId);
                val url = resource.getURL().toExternalForm().concat(encodedId);
                LOGGER.debug("Locating metadata input stream for [{}] via [{}]", encodedId, url);
                val exec = HttpExecutionRequest.builder()
                    .method(HttpMethod.GET)
                    .url(url)
                    .headers(Map.of("Accept", "*/*"))
                    .build();
                response = HttpUtils.execute(exec);
                if (response != null && response.getCode() == HttpStatus.SC_OK) {
                    try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                        return new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
                    }
                }
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            } finally {
                HttpUtils.close(response);
            }
        }
        return ClosedInputStream.INSTANCE;
    }
}
