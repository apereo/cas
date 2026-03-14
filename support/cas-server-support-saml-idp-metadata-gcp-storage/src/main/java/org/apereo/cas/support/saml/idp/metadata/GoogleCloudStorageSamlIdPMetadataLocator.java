package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link GoogleCloudStorageSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class GoogleCloudStorageSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    private final Storage storage;

    public GoogleCloudStorageSamlIdPMetadataLocator(
        final CipherExecutor<String, String> metadataCipherExecutor,
        final Cache<String, SamlIdPMetadataDocument> metadataCache,
        final Storage storage,
        final ConfigurableApplicationContext applicationContext) {
        super(metadataCipherExecutor, metadataCache, applicationContext);
        this.storage = storage;
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        val appliesTo = getAppliesToFor(registeredService).toLowerCase(Locale.ROOT);
        val blob = storage.get(BlobId.of(appliesTo, SamlIdPMetadataDocument.class.getSimpleName()));
        if (blob != null) {
            val content = blob.getContent();
            val body = new String(content, StandardCharsets.UTF_8);
            return SamlIdPMetadataDocument.fromJson(body);
        }
        return null;
    }
}

