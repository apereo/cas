package org.apereo.cas.support.saml.idp.metadata.locator;

import module java.base;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Policy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 * This is {@link AbstractSamlIdPMetadataLocator}.
 * <p>
 * Metadata documents are cached per service definition and globally. A service definition without a document of its
 * own is served the global document, and is remembered as such for as long as documents are cached, apart from the
 * documents themselves: it neither takes a place in the document cache nor queries the backing store again.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@Slf4j
public abstract class AbstractSamlIdPMetadataLocator implements SamlIdPMetadataLocator {

    private static final String CACHE_KEY_METADATA = "CasSamlIdentityProviderMetadata";

    private static final int SERVICES_USING_GLOBAL_METADATA_MAXIMUM_SIZE = 10_000;

    protected final CipherExecutor<String, String> metadataCipherExecutor;

    private final Cache<String, SamlIdPMetadataDocument> metadataCache;

    private final ApplicationContext applicationContext;

    private final Cache<String, Boolean> servicesUsingGlobalMetadata;

    protected AbstractSamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                             final Cache<String, SamlIdPMetadataDocument> metadataCache,
                                             final ApplicationContext applicationContext) {
        this.metadataCipherExecutor = metadataCipherExecutor;
        this.metadataCache = metadataCache;
        this.applicationContext = applicationContext;
        val expiration = Optional.ofNullable(metadataCache.policy())
            .flatMap(Policy::expireAfterWrite)
            .map(Policy.FixedExpiration::getExpiresAfter)
            .orElseGet(() -> Duration.ofHours(1));
        this.servicesUsingGlobalMetadata = Caffeine.newBuilder()
            .maximumSize(SERVICES_USING_GLOBAL_METADATA_MAXIMUM_SIZE)
            .expireAfterWrite(expiration)
            .build();
    }

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

    /**
     * {@inheritDoc}
     * <p>
     * A service definition whose document turns out to be the global one, because the backing store fell back onto it,
     * is answered with the cached global document and remembered as using it, rather than caching a copy of the global
     * document under its own key. Past as many service definitions as the document cache holds, those copies used to
     * evict each other and the global document, and every response went back to the backing store.
     */
    @Override
    public SamlIdPMetadataDocument fetch(final Optional<SamlRegisteredService> registeredService) {
        if (registeredService.isEmpty()) {
            return fetchAndCache(registeredService, null);
        }
        val key = buildCacheKey(registeredService);
        val globalDocument = fetchAndCache(Optional.empty(), null);
        if (servicesUsingGlobalMetadata.getIfPresent(key) != null) {
            return globalDocument;
        }
        val document = fetchAndCache(registeredService, globalDocument);
        return document == null && servicesUsingGlobalMetadata.getIfPresent(key) != null ? globalDocument : document;
    }

    private SamlIdPMetadataDocument fetchAndCache(final Optional<SamlRegisteredService> registeredService,
                                                  final @Nullable SamlIdPMetadataDocument globalDocument) {
        val key = buildCacheKey(registeredService);
        return getMetadataCache().get(key, Unchecked.function(_ -> {
            val metadataDocument = fetchInternal(registeredService);
            if (metadataDocument != null && metadataDocument.isValid()) {
                if (isSameDocument(metadataDocument, globalDocument)) {
                    LOGGER.trace("Service definition [{}] uses the global SAML IdP metadata document", key);
                    servicesUsingGlobalMetadata.put(key, Boolean.TRUE);
                    return null;
                }
                LOGGER.trace("Fetched and cached SAML IdP metadata document [{}] under key [{}]", metadataDocument, key);
                return metadataDocument;
            }

            LOGGER.trace("SAML IdP metadata document [{}] is considered invalid", metadataDocument);
            return null;
        }));
    }

    private static boolean isSameDocument(final SamlIdPMetadataDocument document, final @Nullable SamlIdPMetadataDocument globalDocument) {
        return globalDocument != null
            && Objects.equals(document.getMetadata(), globalDocument.getMetadata())
            && Objects.equals(document.getSigningCertificate(), globalDocument.getSigningCertificate())
            && Objects.equals(document.getSigningKey(), globalDocument.getSigningKey())
            && Objects.equals(document.getEncryptionCertificate(), globalDocument.getEncryptionCertificate())
            && Objects.equals(document.getEncryptionKey(), globalDocument.getEncryptionKey());
    }

    protected Resource resolveContentToResource(final String decodedContent) {
        if (CasConfigurationJasyptCipherExecutor.isValueEncrypted(decodedContent)) {
            val cipher = new CasConfigurationJasyptCipherExecutor(applicationContext.getEnvironment());
            return new ByteArrayResource(cipher.decryptValue(decodedContent).getBytes(StandardCharsets.UTF_8));
        }
        return getResource(decodedContent);
    }

    protected abstract @Nullable SamlIdPMetadataDocument fetchInternal(Optional<SamlRegisteredService> registeredService) throws Exception;
}
