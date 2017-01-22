package org.apereo.cas.configuration.model.support.jpa.ticketregistry;

import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties class for ticketreg.database.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class JpaTicketRegistryProperties extends AbstractJpaProperties {

    /** Default lock timeout is 1 hour. */
    public static final String DEFAULT_LOCK_TIMEOUT = "PT1H";
    
    private boolean jpaLockingTgtEnabled = true;
    
    private String jpaLockingTimeout = DEFAULT_LOCK_TIMEOUT;

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

    public long getJpaLockingTimeout() {
        return Beans.newDuration(jpaLockingTimeout).getSeconds();
    }

    public void setJpaLockingTimeout(final String jpaLockingTimeout) {
        this.jpaLockingTimeout = jpaLockingTimeout;
    }
}
