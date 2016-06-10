package org.apereo.cas.configuration.model.core.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for <code>ticket</code>.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class TicketProperties extends AbstractCryptographyProperties {
    
    private Registry registry;

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(final Registry registry) {
        this.registry = registry;
    }

    public static class Registry {
        private Cleaner cleaner;
        
        public Registry() {
        }

        public Cleaner getCleaner() {
            return cleaner;
        }

        public void setCleaner(final Cleaner cleaner) {
            this.cleaner = cleaner;
        }

        public static class Cleaner {
            private boolean enabled;
            private int startDelay;
            private int repeatInterval;
            
            public Cleaner() {
            }


            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(final boolean enabled) {
                this.enabled = enabled;
            }

            public int getStartDelay() {
                return startDelay;
            }

            public void setStartDelay(final int startDelay) {
                this.startDelay = startDelay;
            }

            public int getRepeatInterval() {
                return repeatInterval;
            }

            public void setRepeatInterval(final int repeatInterval) {
                this.repeatInterval = repeatInterval;
            }
        }
    }
}
