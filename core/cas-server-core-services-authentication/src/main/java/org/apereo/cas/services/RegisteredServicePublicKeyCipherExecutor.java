package org.apereo.cas.services;

import org.apereo.cas.util.EncodingUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Security;
import java.util.Optional;

/**
 * Default cipher implementation based on public keys.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public class RegisteredServicePublicKeyCipherExecutor implements RegisteredServiceCipherExecutor {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Encode internally, meant to be called by extensions.
     * Default behavior will encode the data based on the
     * registered service public key's algorithm using {@link javax.crypto.Cipher}.
     *
     * @param data              the data
     * @param publicKey         the public key
     * @param registeredService the registered service
     * @return a byte[] that contains the encrypted result
     */
    @SneakyThrows
    protected static byte[] encodeInternal(final String data, final PublicKey publicKey,
                                           final RegisteredService registeredService) {
        val cipher = initializeCipherBasedOnServicePublicKey(publicKey, registeredService);
        if (cipher != null) {
            LOGGER.trace("Initialized cipher successfully. Proceeding to finalize...");
            return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }

    /**
     * Create registered service public key defined.
     *
     * @param registeredService the registered service
     * @return the public key
     */
    private static PublicKey createRegisteredServicePublicKey(final RegisteredService registeredService) {
        if (registeredService.getPublicKey() == null) {
            LOGGER.trace("No public key is defined for service [{}]. No encoding will take place.", registeredService);
            return null;
        }
        val publicKey = registeredService.getPublicKey().createInstance();
        if (publicKey == null) {
            LOGGER.trace("No public key created for service [{}]. No encoding will take place.", registeredService);
            return null;
        }
        return publicKey;
    }

    /**
     * Initialize cipher based on service public key.
     *
     * @param publicKey         the public key
     * @param registeredService the registered service
     * @return the false if no public key is found
     * or if cipher cannot be initialized, etc.
     */
    private static Cipher initializeCipherBasedOnServicePublicKey(final PublicKey publicKey,
                                                                  final RegisteredService registeredService) {
        try {
            LOGGER.debug("Using service [{}] public key [{}] to initialize the cipher", registeredService.getServiceId(),
                registeredService.getPublicKey());

            val cipher = Cipher.getInstance(publicKey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            LOGGER.trace("Initialized cipher in encrypt-mode via the public key algorithm [{}] for service [{}]",
                publicKey.getAlgorithm(), registeredService.getServiceId());
            return cipher;
        } catch (final Exception e) {
            LOGGER.warn("Cipher could not be initialized for service [{}]. Error [{}]",
                registeredService, e.getMessage());
        }
        return null;
    }

    /**
     * Encrypt using the given cipher associated with the service,
     * and encode the data in base 64.
     *
     * @param data    the data
     * @param service the registered service
     * @return the encoded piece of data in base64
     */
    @Override
    public String encode(final String data, final Optional<RegisteredService> service) {
        try {
            if (service.isPresent()) {
                val registeredService = service.get();
                val publicKey = createRegisteredServicePublicKey(registeredService);
                val result = encodeInternal(data, publicKey, registeredService);
                if (result != null) {
                    return EncodingUtils.encodeBase64(result);
                }
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String decode(final String data, final Optional<RegisteredService> service) {
        LOGGER.warn("Operation is not supported by this cipher");
        return null;
    }
}
