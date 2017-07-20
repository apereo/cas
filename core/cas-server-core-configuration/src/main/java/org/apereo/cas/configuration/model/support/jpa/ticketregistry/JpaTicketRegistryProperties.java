package org.apereo.cas.configuration.model.support.jpa.ticketregistry;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.persistence.LockModeType;

/**
 * Common properties for jpa ticket reg.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class JpaTicketRegistryProperties extends AbstractJpaProperties {

    /**
     * Default lock timeout is 1 hour.
     */
    public static final String DEFAULT_LOCK_TIMEOUT = "PT1H";

    private static final long serialVersionUID = -8053839523783801072L;

    private LockModeType ticketLockType = LockModeType.NONE;

    private String jpaLockingTimeout = DEFAULT_LOCK_TIMEOUT;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public JpaTicketRegistryProperties() {
        super.setUrl("jdbc:hsqldb:mem:cas-ticket-registry");
    }

    public EncryptionRandomizedSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionRandomizedSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }

    public long getJpaLockingTimeout() {
        return Beans.newDuration(jpaLockingTimeout).getSeconds();
    }

    public void setJpaLockingTimeout(final String jpaLockingTimeout) {
        this.jpaLockingTimeout = jpaLockingTimeout;
    }

    public LockModeType getTicketLockType() {
        return ticketLockType;
    }

    public void setTicketLockType(final LockModeType ticketLockType) {
        this.ticketLockType = ticketLockType;
    }
}
