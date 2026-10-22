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
import org.jspecify.annotations.Nullable;
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

    /**
     * Fetch the metadata document for the service, or the global document when the service has none of its own.
     * Metadata is only generated per service when asked for, so a service without its own document must use the
     * identity provider's global metadata and keys, as the JPA, MongoDb and Redis locators do; without this fallback
     * the signing key of every service provider resolves to nothing and SAML2 responses cannot be signed.
     *
     * @param registeredService the registered service
     * @return the metadata document, or null if there is none
     */
    @Override
    public @Nullable SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        if (registeredService.isPresent()) {
            val document = findDocumentFor(registeredService);
            if (document != null && document.isValid()) {
                return document;
            }
        }
        return findDocumentFor(Optional.empty());
    }

    private @Nullable SamlIdPMetadataDocument findDocumentFor(final Optional<SamlRegisteredService> registeredService) {
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

