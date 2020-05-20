package org.apereo.cas.services;

import org.apereo.cas.util.EncodingUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
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
                val result = encodeInternal(data, registeredService);
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

    /**
     * Encode internally, meant to be called by extensions.
     * Default behavior will encode the data based on the
     * registered service public key's algorithm using {@link javax.crypto.Cipher}.
     *
     * @param data              the data
     * @param registeredService the registered service
     * @return a byte[] that contains the encrypted result
     */
    @SneakyThrows
    protected static byte[] encodeInternal(final String data, final RegisteredService registeredService) {
        val publicKey = registeredService.getPublicKey();
        if (publicKey == null) {
            LOGGER.error("No public key is defined for service [{}]. No attributes will be released", registeredService);
            return null;
        }
        LOGGER.debug("Using service [{}] public key [{}] to initialize the cipher", registeredService.getServiceId(), publicKey);
        val cipher = publicKey.toCipher();
        if (cipher != null) {
            LOGGER.trace("Initialized cipher successfully. Proceeding to finalize...");
            return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }
}
