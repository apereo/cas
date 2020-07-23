package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * This is {@link AbstractSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractSamlIdPMetadataLocator implements SamlIdPMetadataLocator {

    private static final String CACHE_KEY_METADATA = "CasSamlIdentityProviderMetadata";

    /**
     * Cipher executor to encrypt/sign metadata.
     */
    protected final CipherExecutor<String, String> metadataCipherExecutor;

    private Cache<String, SamlIdPMetadataDocument> metadataCache;

    private static Resource getResource(final String data) {
        if (StringUtils.isBlank(data)) {
            LOGGER.warn("Cannot determine resource based on blank/empty data");
            return ResourceUtils.EMPTY_RESOURCE;
        }
        return new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String buildCacheKey(final Optional<SamlRegisteredService> registeredService) {
        if (registeredService.isEmpty()) {
            LOGGER.trace("No registered service provided; using default cache key for metadata");
            return CACHE_KEY_METADATA;
        }
        val samlRegisteredService = registeredService.get();
        val key = CACHE_KEY_METADATA + '_' + samlRegisteredService.getId() + '_' + samlRegisteredService.getName();
        LOGGER.trace("Using {} as cache key for metadata for service definition", key);
        return key;
    }

    @Override
    public Resource resolveSigningCertificate(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            LOGGER.trace("Fetching signing certificate resource for metadata document [{}]", metadataDocument.getId());
            return getResource(metadataDocument.getSigningCertificateDecoded());
        }
        return ResourceUtils.EMPTY_RESOURCE;
    }

    @Override
    public Resource resolveSigningKey(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            val data = metadataDocument.getSigningKey();
            LOGGER.trace("Fetching signing key resource for metadata document [{}]", metadataDocument.getId());
            return getResource(metadataCipherExecutor.decode(data));
        }
        return ResourceUtils.EMPTY_RESOURCE;
    }

    @Override
    public Resource resolveMetadata(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            LOGGER.trace("Fetching metadata resource for metadata document [{}]", metadataDocument.getId());
            return getResource(metadataDocument.getMetadataDecoded());
        }
        return ResourceUtils.EMPTY_RESOURCE;
    }

    @Override
    public Resource getEncryptionCertificate(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            LOGGER.trace("Fetching encryption certificate resource for metadata document [{}]", metadataDocument.getId());
            return getResource(metadataDocument.getEncryptionCertificateDecoded());
        }
        return ResourceUtils.EMPTY_RESOURCE;
    }

    @Override
    public Resource resolveEncryptionKey(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            val data = metadataDocument.getEncryptionKey();
            LOGGER.trace("Fetching encryption key resource for metadata document [{}]", metadataDocument.getId());
            return getResource(metadataCipherExecutor.decode(data));
        }
        return ResourceUtils.EMPTY_RESOURCE;
    }

    @Override
    public boolean exists(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = fetch(registeredService);
        return metadataDocument != null && metadataDocument.isValid();
    }

    @Override
    public final SamlIdPMetadataDocument fetch(final Optional<SamlRegisteredService> registeredService) {
        initializeCache();

        val key = buildCacheKey(registeredService);

        return metadataCache.get(key, k -> {
            val metadataDocument = fetchInternal(registeredService);
            if (metadataDocument != null && metadataDocument.isValid()) {
                LOGGER.trace("Fetched and cached SAML IdP metadata document [{}] under key [{}]", metadataDocument, key);
                return metadataDocument;
            }

            LOGGER.trace("SAML IdP metadata document [{}] is considered invalid", metadataDocument);
            return null;
        });
    }

    /**
     * Fetch saml idp metadata document.
     *
     * @param registeredService the registered service
     * @return the saml idp metadata document
     */
    protected abstract SamlIdPMetadataDocument fetchInternal(Optional<SamlRegisteredService> registeredService);

    private void initializeCache() {
        if (metadataCache == null) {
            metadataCache = Caffeine.newBuilder()
                .initialCapacity(1)
                .maximumSize(100)
                .expireAfterAccess(Duration.ofHours(1))
                .build();
        }
    }
}
