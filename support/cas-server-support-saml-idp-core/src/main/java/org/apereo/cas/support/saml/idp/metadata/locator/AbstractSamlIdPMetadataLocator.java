package org.apereo.cas.support.saml.idp.metadata.locator;

import module java.base;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 * This is {@link AbstractSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public abstract class AbstractSamlIdPMetadataLocator implements SamlIdPMetadataLocator {

    private static final String CACHE_KEY_METADATA = "CasSamlIdentityProviderMetadata";

    protected final CipherExecutor<String, String> metadataCipherExecutor;

    private final Cache<@NonNull String, SamlIdPMetadataDocument> metadataCache;

    private final ApplicationContext applicationContext;
    
    private static Resource getResource(final String data) {
        return new ByteArrayResource(StringUtils.defaultString(data).getBytes(StandardCharsets.UTF_8));
    }

    private static String buildCacheKey(final Optional<SamlRegisteredService> registeredService) {
        if (registeredService.isEmpty()) {
            LOGGER.trace("No registered service provided; using default cache key for metadata");
            return CACHE_KEY_METADATA;
        }
        val samlRegisteredService = registeredService.get();
        val key = CACHE_KEY_METADATA + '_' + samlRegisteredService.getId() + '_' + samlRegisteredService.getName();
        LOGGER.trace("Using [{}] as cache key for metadata for service definition", key);
        return key;
    }

    @Override
    public Resource resolveSigningCertificate(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            LOGGER.trace("Fetching signing certificate resource for metadata document [{}]", metadataDocument.getId());
            return getResource(metadataDocument.getSigningCertificate());
        }
        return ResourceUtils.EMPTY_RESOURCE;
    }

    @Override
    public Resource resolveSigningKey(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            val data = metadataDocument.getSigningKey();
            LOGGER.trace("Fetching signing key resource for metadata document [{}]", metadataDocument.getId());
            val decodedKeyContent = metadataCipherExecutor.decode(data);
            return resolveContentToResource(decodedKeyContent);
        }
        return ResourceUtils.EMPTY_RESOURCE;
    }

    @Override
    public Resource resolveMetadata(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            LOGGER.trace("Fetching metadata resource for metadata document [{}]", metadataDocument.getId());
            return getResource(metadataDocument.getMetadata());
        }
        return ResourceUtils.EMPTY_RESOURCE;
    }

    @Override
    public Resource resolveEncryptionCertificate(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            LOGGER.trace("Fetching encryption certificate resource for metadata document [{}]", metadataDocument.getId());
            return getResource(metadataDocument.getEncryptionCertificate());
        }
        return ResourceUtils.EMPTY_RESOURCE;
    }

    @Override
    public Resource resolveEncryptionKey(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        val metadataDocument = fetch(registeredService);
        if (metadataDocument != null && metadataDocument.isValid()) {
            val data = metadataDocument.getEncryptionKey();
            LOGGER.trace("Fetching encryption key resource for metadata document [{}]", metadataDocument.getId());
            val decodedKeyContent = metadataCipherExecutor.decode(data);
            return resolveContentToResource(decodedKeyContent);
        }
        return ResourceUtils.EMPTY_RESOURCE;
    }
    
    @Override
    public boolean exists(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        val metadataDocument = fetch(registeredService);
        return metadataDocument != null && metadataDocument.isValid();
    }

    @Override
    public SamlIdPMetadataDocument fetch(final Optional<SamlRegisteredService> registeredService) {
        val key = buildCacheKey(registeredService);

        return getMetadataCache().get(key, Unchecked.function(_ -> {
            val metadataDocument = fetchInternal(registeredService);
            if (metadataDocument != null && metadataDocument.isValid()) {
                LOGGER.trace("Fetched and cached SAML IdP metadata document [{}] under key [{}]", metadataDocument, key);
                return metadataDocument;
            }

            LOGGER.trace("SAML IdP metadata document [{}] is considered invalid", metadataDocument);
            return null;
        }));
    }

    protected Resource resolveContentToResource(final String decodedContent) {
        if (CasConfigurationJasyptCipherExecutor.isValueEncrypted(decodedContent)) {
            val cipher = new CasConfigurationJasyptCipherExecutor(applicationContext.getEnvironment());
            return new ByteArrayResource(cipher.decryptValue(decodedContent).getBytes(StandardCharsets.UTF_8));
        }
        return getResource(decodedContent);
    }

    protected abstract SamlIdPMetadataDocument fetchInternal(Optional<SamlRegisteredService> registeredService) throws Exception;
}
