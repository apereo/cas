package org.apereo.cas.configuration.model.webapp;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Configuration properties class for webflow.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class WebflowProperties {

    /**
     * Encryption/signing setting for webflow.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    /**
     * Whether CAS should take control of all spring webflow modifications
     * and dynamically alter views, states and actions.
     */
    private boolean autoconfigure = true;

    /**
     * Whether webflow should remain in "live reload" mode, able to auto detect
     * changes and react. This is useful if the location of the webflow is externalized
     * and changes are done ad-hoc to the webflow to accommodate changes.
     */
    private boolean refresh;

    /**
     * Whether flow executions should redirect after they pause before rendering.
     */
    private boolean alwaysPauseRedirect;

    /**
     * Whether flow executions redirect after they pause for transitions that remain in the same view state.
     */
    private boolean redirectSameState;

    /**
     * Webflow session management settings.
     */
    private Session session = new Session();

    public EncryptionJwtSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionJwtSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }

    public boolean isAutoconfigure() {
        return autoconfigure;
    }

    public void setAutoconfigure(final boolean autoconfigure) {
        this.autoconfigure = autoconfigure;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(final boolean refresh) {
        this.refresh = refresh;
    }

    public boolean isAlwaysPauseRedirect() {
        return alwaysPauseRedirect;
    }

    public void setAlwaysPauseRedirect(final boolean alwaysPauseRedirect) {
        this.alwaysPauseRedirect = alwaysPauseRedirect;
    }

    public boolean isRedirectSameState() {
        return redirectSameState;
    }

    public void setRedirectSameState(final boolean redirectSameState) {
        this.redirectSameState = redirectSameState;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(final Session session) {
        this.session = session;
    }

    /**
     * The Webflow Session settings.
     */
    public static class Session {
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
         * If sessions are to be replicated via Hazelcast, defines the location of a <code>hazelcast.xml</code>
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
}
