package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import module java.xml;
import org.apereo.cas.util.LoggingUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.metadata.resolver.impl.AbstractBatchMetadataResolver;

/**
 * This is {@link BaseElementMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Slf4j
public abstract class BaseElementMetadataResolver extends AbstractBatchMetadataResolver {
    protected Element metadataRootElement;

    /**
     * Sets metadata root element and reinitialize the backing store.
     *
     * @param metadataRootElement the metadata root element
     */
    public void setMetadataRootElement(final Element metadataRootElement) {
        this.metadataRootElement = metadataRootElement;
        try {
            val unmarshaller = getUnmarshallerFactory().ensureUnmarshaller(metadataRootElement);
            val metadataTemp = unmarshaller.unmarshall(metadataRootElement);
            val newBackingStore = preProcessNewMetadata(metadataTemp);
            releaseMetadataDOM(metadataTemp);
            setBackingStore(newBackingStore);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }
}
