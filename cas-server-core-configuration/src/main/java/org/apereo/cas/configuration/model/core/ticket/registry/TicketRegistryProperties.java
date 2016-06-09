package org.apereo.cas.configuration.model.core.ticket.registry;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link TicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "ticket.registry", ignoreUnknownFields = false)
public class TicketRegistryProperties {
    private InMemory inMemory = new InMemory();
    private Cleaner cleaner = new Cleaner();

    public InMemory getInMemory() {
        return inMemory;
    }

    public void setInMemory(final InMemory inMemory) {
        this.inMemory = inMemory;
    }

    public Cleaner getCleaner() {
        return cleaner;
    }

    public void setCleaner(final Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    public static class InMemory {
        private int initialCapacity = 1000;
        private int loadFactor = 1;
        private int concurrency = 20;

        public int getInitialCapacity() {
            return initialCapacity;
        }

        public void setInitialCapacity(final int initialCapacity) {
            this.initialCapacity = initialCapacity;
        }

        public int getLoadFactor() {
            return loadFactor;
        }

        public void setLoadFactor(final int loadFactor) {
            this.loadFactor = loadFactor;
        }

        public int getConcurrency() {
            return concurrency;
        }

        public void setConcurrency(final int concurrency) {
            this.concurrency = concurrency;
        }
    }
    
    public static class Cleaner {
        private boolean enabled = true;
        private long startDelay = 20000;
        private long repeatInterval = 10000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public long getStartDelay() {
            return startDelay;
        }

        public void setStartDelay(final long startDelay) {
            this.startDelay = startDelay;
        }

        public long getRepeatInterval() {
            return repeatInterval;
        }

        public void setRepeatInterval(final long repeatInterval) {
            this.repeatInterval = repeatInterval;
        }
    }
}
