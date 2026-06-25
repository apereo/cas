package org.apereo.cas.services;

import module java.base;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * This is {@link CasGoogleCloudStorageSubscriptionCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@FunctionalInterface
public interface CasGoogleCloudStorageSubscriptionCustomizer {
    /**
     * Customize subscriber . builder.
     *
     * @param builder the builder
     * @return the subscriber . builder
     */
    @CanIgnoreReturnValue
    Subscriber.Builder customize(Subscriber.Builder builder);

    /**
     * No-op cas google cloud storage subscription customizer.
     *
     * @return the cas google cloud storage subscription customizer
     */
    static CasGoogleCloudStorageSubscriptionCustomizer noOp() {
        return builder -> builder;
    }
}
