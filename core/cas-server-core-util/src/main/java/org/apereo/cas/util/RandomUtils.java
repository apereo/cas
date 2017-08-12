package org.apereo.cas.util;

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
    private RandomUtils() {
    }

    /**
     * Get strong SecureRandom instance from +securerandom.strongAlgorithms+ and wrap the checked exception.
     *
     * @return the strong instance
     */
    public static SecureRandom getInstanceStrong() {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Get strong enough SecureRandom instance and wrap the checked exception.
     *
     * @return the strong instance
     */
    public static SecureRandom getInstanceGood() {
        try {
            return SecureRandom.getInstance("NativePRNGNonBlocking");
        } catch (final NoSuchAlgorithmException e) {
            return new SecureRandom();
        }
    }
}
