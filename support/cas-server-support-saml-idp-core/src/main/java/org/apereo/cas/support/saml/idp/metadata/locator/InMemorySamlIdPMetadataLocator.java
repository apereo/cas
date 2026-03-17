package org.apereo.cas.support.saml.idp.metadata.locator;

import module java.base;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.security.util.InMemoryResource;
import org.springframework.util.Assert;

/**
 * This is {@link InMemorySamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@Getter
@Monitorable
public class InMemorySamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    private final SamlIdPMetadataDocument document;

    public InMemorySamlIdPMetadataLocator(final CipherExecutor cipherExecutor,
                                          final SamlIdPMetadataDocument document,
                                          final Cache<String, SamlIdPMetadataDocument> metadataCache,
                                          final ConfigurableApplicationContext applicationContext) {
        super(cipherExecutor, metadataCache, applicationContext);
        this.document = document;
        validateDocument(document);
    }


    @Override
    public Resource resolveSigningCertificate(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        return new InMemoryResource(document.getSigningCertificate());
    }

    @Override
    public Resource resolveSigningKey(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        return new InMemoryResource(document.getSigningKey());
    }

    @Override
    public Resource resolveMetadata(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        return new InMemoryResource(document.getMetadata());
    }

    @Override
    public Resource resolveEncryptionCertificate(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        return new InMemoryResource(document.getEncryptionCertificate());
    }

    @Override
    public Resource resolveEncryptionKey(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        return new InMemoryResource(document.getEncryptionKey());
    }

    @Override
    public boolean exists(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        return resolveMetadata(registeredService).exists();
    }

    @Override
    protected SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) throws Exception {
        validateDocument(document);
        return this.document;
    }

    private static void validateDocument(final SamlIdPMetadataDocument document) {
        Assert.isTrue(document.isValid(), "SamlIdPMetadataDocument is invalid and cannot be used");
    }
}
