package org.apereo.cas.support.saml.idp.metadata.locator;

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

    /**
     * The idp metadata document fetched from storage.
     */
    protected SamlIdPMetadataDocument metadataDocument = new SamlIdPMetadataDocument();

    private Cache<String, SamlIdPMetadataDocument> metadataCache;

    @Override
    public Resource getSigningCertificate() {
        if (exists()) {
            val cert = metadataDocument.getSigningCertificateDecoded();
            return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
        }
        return null;
    }

    @Override
    public Resource getSigningKey() {
        if (exists()) {
            val data = metadataDocument.getSigningKey();
            val cert = metadataCipherExecutor.decode(data);
            return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
        }
        return null;
    }

    @Override
    public Resource getMetadata() {
        if (exists()) {
            val data = metadataDocument.getMetadataDecoded();
            return new InputStreamResource(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
        }
        return null;
    }

    @Override
    public Resource getEncryptionCertificate() {
        if (exists()) {
            val cert = metadataDocument.getEncryptionCertificateDecoded();
            return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
        }
        return null;
    }

    @Override
    public Resource getEncryptionKey() {
        if (exists()) {
            val data = metadataDocument.getEncryptionKey();
            val cert = metadataCipherExecutor.decode(data);
            return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
        }
        return null;
    }

    @Override
    public void initialize() {
        fetch();
    }

    @Override
    public boolean exists() {
        fetch();
        return isMetadataDocumentValid();
    }

    @Override
    public final SamlIdPMetadataDocument fetch() {
        initializeCache();

        val map = metadataCache.asMap();
        if (map.containsKey(CACHE_KEY_METADATA)) {
            return map.get(CACHE_KEY_METADATA);
        }
        val document = fetchInternal();
        if (isMetadataDocumentValid()) {
            map.put(CACHE_KEY_METADATA, this.metadataDocument);
        }
        return document;
    }

    /**
     * Fetch saml idp metadata document.
     *
     * @return the saml id p metadata document
     */
    protected abstract SamlIdPMetadataDocument fetchInternal();

    private boolean isMetadataDocumentValid() {
        return metadataDocument != null && metadataDocument.isValid();
    }

    private void initializeCache() {
        if (metadataCache == null) {
            metadataCache = Caffeine.newBuilder()
                .initialCapacity(1)
                .maximumSize(1)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build();
        }
    }
}
