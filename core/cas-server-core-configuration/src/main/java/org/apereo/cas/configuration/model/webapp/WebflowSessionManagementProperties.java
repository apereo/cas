package org.apereo.cas.configuration.model.webapp;

import org.apereo.cas.configuration.support.Beans;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link WebflowSessionManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class WebflowSessionManagementProperties implements Serializable {
    private static final long serialVersionUID = 7479028707118198914L;
    /**
     * Sets the time period that can elapse before a
     * timeout occurs on an attempt to acquire a conversation lock. The default is 30 seconds.
     * Only relevant if session storage is done on the server.
     */
    private String lockTimeout = "PT30S";
    /**
     * Using the maxConversations property, you can limit the number of concurrently
     * active conversations allowed in a single session. If the maximum is exceeded,
     * the conversation manager will automatically end the oldest conversation.
     * The default is 5, which should be fine for most situations.
     * Set it to -1 for no limit. Setting maxConversations
     * to 1 allows easy resource cleanup in situations where there
     * should only be one active conversation per session.
     * Only relevant if session storage is done on the server.
     */
    private int maxConversations = 5;
    /**
     * Whether or not the snapshots should be compressed.
     */
    private boolean compress;

    /**
     * Controls whether spring webflow sessions are to be stored server-side or client side.
     * By default state is managed on the client side, that is also signed and encrypted.
     */
    private boolean storage;

    /**
     * If sessions are to be replicated via Hazelcast, defines the location of a {@code hazelcast.xml}
     * file that defines how state should be replicated.
     * Only relevant if session storage is done on the server.
     */
    private Resource hzLocation = new ClassPathResource("hazelcast.xml");

    public long getLockTimeout() {
        return Beans.newDuration(lockTimeout).getSeconds();
    }

    public void setLockTimeout(final String lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    public int getMaxConversations() {
        return maxConversations;
    }

    public void setMaxConversations(final int maxConversations) {
        this.maxConversations = maxConversations;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(final boolean compress) {
        this.compress = compress;
    }

    public boolean isStorage() {
        return storage;
    }

    public void setStorage(final boolean storage) {
        this.storage = storage;
    }

    public Resource getHzLocation() {
        return hzLocation;
    }

    public void setHzLocation(final Resource hzLocation) {
        this.hzLocation = hzLocation;
    }
}
