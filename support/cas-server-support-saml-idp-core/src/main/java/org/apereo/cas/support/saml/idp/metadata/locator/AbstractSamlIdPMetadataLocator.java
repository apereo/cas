package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link AbstractSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public abstract class AbstractSamlIdPMetadataLocator implements SamlIdPMetadataLocator {

    private static final String CACHE_KEY_METADATA = "CasSamlIdentityProviderMetadata";

    /**
     * Cipher executor to encrypt/sign metadata.
     */
    protected final CipherExecutor<String, String> metadataCipherExecutor;

    private Cache<String, SamlIdPMetadataDocument> metadataCache;

    private static Resource getResource(final String data) {
        return new InputStreamResource(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Resource resolveSigningCertificate(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            return getResource(metadataDocument.getSigningCertificateDecoded());
        }
        return null;
    }

    @Override
    public Resource resolveSigningKey(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            val data = metadataDocument.getSigningKey();
            return getResource(metadataCipherExecutor.decode(data));
        }
        return null;
    }

    @Override
    public Resource resolveMetadata(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            return getResource(metadataDocument.getMetadataDecoded());
        }
        return null;
    }

    @Override
    public Resource getEncryptionCertificate(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            return getResource(metadataDocument.getEncryptionCertificateDecoded());
        }
        return null;
    }

    @Override
    public Resource resolveEncryptionKey(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            val data = metadataDocument.getEncryptionKey();
            return getResource(metadataCipherExecutor.decode(data));
        }
        return null;
    }

    @Override
    public boolean exists(final Optional<SamlRegisteredService> registeredService) {
        val metadataDocument = fetch(registeredService);
        return metadataDocument != null && metadataDocument.isValid();
    }

    @Override
    public final SamlIdPMetadataDocument fetch(final Optional<SamlRegisteredService> registeredService) {
        initializeCache();

        val map = metadataCache.asMap();
        val key = buildCacheKey(registeredService);

        if (map.containsKey(key)) {
            return map.get(key);
        }
        val metadataDocument = fetchInternal(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            map.put(key, metadataDocument);
        }
        return metadataDocument;
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
                .maximumSize(1)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build();
        }
    }

    private static String buildCacheKey(final Optional<SamlRegisteredService> registeredService) {
        if (registeredService.isEmpty()) {
            return CACHE_KEY_METADATA;
        }
        val samlRegisteredService = registeredService.get();
        return CACHE_KEY_METADATA + '_' + samlRegisteredService.getId() + '_' + samlRegisteredService.getName();
    }
}
