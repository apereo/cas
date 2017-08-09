package org.apereo.cas.services.util;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Security;

/**
 * Default cipher implementation based on public keys.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultRegisteredServiceCipherExecutor implements RegisteredServiceCipherExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRegisteredServiceCipherExecutor.class);

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
    public String encode(final String data, final RegisteredService service) {
        try {
            final PublicKey publicKey = createRegisteredServicePublicKey(service);
            final byte[] result = encodeInternal(data, publicKey, service);
            if (result != null) {
                return EncodingUtils.encodeBase64(result);
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

        return null;
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
    protected static byte[] encodeInternal(final String data, final PublicKey publicKey,
                                           final RegisteredService registeredService) {
        try {
            final Cipher cipher = initializeCipherBasedOnServicePublicKey(publicKey, registeredService);
            if (cipher != null) {
                LOGGER.debug("Initialized cipher successfully. Proceeding to finalize...");
                return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            }
        } catch (final Exception e) {
            throw new RuntimeException("Unable to encode data for service " + registeredService.getServiceId(), e);
        }
        return null;
    }

    /**
     * Create registered service public key defined.
     *
     * @param registeredService the registered service
     * @return the public key
     * @throws Exception the exception, if key cant be created
     */
    private static PublicKey createRegisteredServicePublicKey(final RegisteredService registeredService) throws Exception {
        if (registeredService.getPublicKey() == null) {
            LOGGER.debug("No public key is defined for service [{}]. No encoding will take place.", registeredService);
            return null;
        }
        final PublicKey publicKey = registeredService.getPublicKey().createInstance();
        if (publicKey == null) {
            LOGGER.debug("No public key instance created for service [{}]. No encoding will take place.", registeredService);
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

            final Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            LOGGER.debug("Initialized cipher in encrypt-mode via the public key algorithm [{}] for service [{}]", 
                    publicKey.getAlgorithm(), registeredService.getServiceId());
            return cipher;
        } catch (final Exception e) {
            LOGGER.warn("Cipher could not be initialized for service [{}]. Error [{}]",
                    registeredService, e.getMessage());
        }
        return null;
    }
}
