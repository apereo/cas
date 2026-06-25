package org.apereo.cas.services;

import module java.base;
import com.google.cloud.pubsub.v1.Subscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This is {@link CasGoogleCloudStorageServiceRegistryListener}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class CasGoogleCloudStorageServiceRegistryListener implements InitializingBean, DisposableBean {
    /**
     * Subscription name.
     */
    public static final String SUBSCRIPTION_NAME = "cas-service-regisry-gcp-storage";
    private final Subscriber subscriber;

    @Override
    public void afterPropertiesSet() {
        subscriber.startAsync().awaitRunning();
    }

    @Override
    public void destroy() {
        subscriber.stopAsync().awaitTerminated();
    }
}
