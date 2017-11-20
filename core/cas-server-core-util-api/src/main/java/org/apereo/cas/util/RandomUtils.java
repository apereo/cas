package org.apereo.cas.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * This is {@link RandomUtils}
 * that encapsulates common base64 calls and operations
 * in one spot.
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.2.0
 */
public final class RandomUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomUtils.class);

    private RandomUtils() {
    }

    /**
     * Get strong enough SecureRandom instance and wrap the checked exception.
     * TODO Try {@code NativePRNGNonBlocking} and failover to default SHA1PRNG until Java 9.
     *
     * @return the strong instance
     */
    public static SecureRandom getInstanceNative() {
        try {
            return SecureRandom.getInstance("NativePRNGNonBlocking");
        } catch (final NoSuchAlgorithmException e) {
            LOGGER.trace(e.getMessage(), e);
            return new SecureRandom();
        }
    }
    
}
