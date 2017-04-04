package org.apereo.cas.configuration.model.webapp;

import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Configuration properties class for webflow.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class WebflowProperties extends CryptographyProperties {

    private boolean autoconfigure = true;
    
    private boolean refresh;

    private boolean alwaysPauseRedirect;

    private boolean redirectSameState;

    private Session session = new Session();

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

    public static class Session {
        private String lockTimeout = "PT30S";
        private int maxConversations = 5;
        private boolean compress;
        private boolean storage;
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
