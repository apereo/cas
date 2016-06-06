package org.apereo.cas.configuration.model.support.jpa.ticketregistry;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for ticketreg.database.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "ticketreg.database", ignoreUnknownFields = false)
public class JpaTicketRegistryProperties extends AbstractJpaProperties {

    public JpaTicketRegistryProperties() {
        super.setUrl("jdbc:hsqldb:mem:cas-ticket-registry");
    }

    private boolean jpaLockingTgtEnabled = true;

    public boolean isJpaLockingTgtEnabled() {
        return jpaLockingTgtEnabled;
    }

    public void setJpaLockingTgtEnabled(final boolean jpaLockingTgtEnabled) {
        this.jpaLockingTgtEnabled = jpaLockingTgtEnabled;
    }
}
