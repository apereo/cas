package org.apereo.cas.configuration.model.core;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for events.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "events", ignoreUnknownFields = false)
public class EventsProperties {

    private boolean trackGeolocation = false;

    private Jpa jpa = new Jpa();

    public boolean isTrackGeolocation() {
        return trackGeolocation;
    }

    public void setTrackGeolocation(final boolean trackGeolocation) {
        this.trackGeolocation = trackGeolocation;
    }

    public Jpa getJpa() {
        return jpa;
    }

    public void setJpa(final Jpa jpa) {
        this.jpa = jpa;
    }

    public static class Jpa {

        private Database database = new Database();

        public Database getDatabase() {
            return database;
        }

        public void setDatabase(final Database database) {
            this.database = database;
        }

        public static class Database extends AbstractJpaProperties {
            public Database() {
                super.setUrl("jdbc:hsqldb:mem:cas-events");
            }
        }

    }



}
