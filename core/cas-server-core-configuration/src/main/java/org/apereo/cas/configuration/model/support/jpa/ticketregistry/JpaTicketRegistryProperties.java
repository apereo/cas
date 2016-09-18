package org.apereo.cas.configuration.model.support.jpa.ticketregistry;

import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties class for ticketreg.database.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class JpaTicketRegistryProperties extends AbstractJpaProperties {

    /** Default lock timeout is 1 hour. */
    public static final int DEFAULT_LOCK_TIMEOUT = 3600;
    
    private boolean jpaLockingTgtEnabled = true;
    
    private int jpaLockingTimeout = DEFAULT_LOCK_TIMEOUT;

    @NestedConfigurationProperty
    private CryptographyProperties crypto = new CryptographyProperties();

    public JpaTicketRegistryProperties() {
        super.setUrl("jdbc:hsqldb:mem:cas-ticket-registry");
    }
    
    public CryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final CryptographyProperties crypto) {
        this.crypto = crypto;
    }
    
    public boolean isJpaLockingTgtEnabled() {
        return jpaLockingTgtEnabled;
    }

    public void setJpaLockingTgtEnabled(final boolean jpaLockingTgtEnabled) {
        this.jpaLockingTgtEnabled = jpaLockingTgtEnabled;
    }

    public int getJpaLockingTimeout() {
        return jpaLockingTimeout;
    }

    public void setJpaLockingTimeout(final int jpaLockingTimeout) {
        this.jpaLockingTimeout = jpaLockingTimeout;
    }
}
