package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Default cipher implementation based on public keys.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public class RegisteredServicePublicKeyCipherExecutor implements RegisteredServiceCipherExecutor {
    /**
     * Instantiates a new registered service cipher executor.
     */
    public static final RegisteredServiceCipherExecutor INSTANCE = new RegisteredServicePublicKeyCipherExecutor();

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

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
            return FunctionUtils.doUnchecked(() -> cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        }
        return null;
    }

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
            LoggingUtils.warn(LOGGER, e);
        }
        return null;
    }

    @Override
    public String decode(final String data, final Optional<RegisteredService> service) {
        LOGGER.warn("Operation is not supported by this cipher");
        return null;
    }
}
