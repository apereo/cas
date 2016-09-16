package org.apereo.cas.configuration.model.core.events;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;

/**
 * Configuration properties class for events.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class EventsProperties {

    private boolean trackGeolocation;

    private Jpa jpa = new Jpa();

    private Mongodb mongodb = new Mongodb();

    public Mongodb getMongodb() {
        return mongodb;
    }

    public void setMongodb(final Mongodb mongodb) {
        this.mongodb = mongodb;
    }

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

    public static class Jpa extends AbstractJpaProperties {
    }

    public static class Mongodb {
        private String clientUri = "";
        private String collection = "MongoDbCasEventRepository";
        private boolean dropCollection;

        public String getClientUri() {
            return clientUri;
        }

        public void setClientUri(final String clientUri) {
            this.clientUri = clientUri;
        }

        public String getCollection() {
            return collection;
        }

        public void setCollection(final String collection) {
            this.collection = collection;
        }

        public boolean isDropCollection() {
            return dropCollection;
        }

        public void setDropCollection(final boolean dropCollection) {
            this.dropCollection = dropCollection;
        }
    }
}
