package org.apereo.cas.configuration.model.support.mongo.ticketregistry;

import org.apereo.cas.configuration.model.support.mongo.AbstractMongoInstanceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link MongoTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class MongoTicketRegistryProperties extends AbstractMongoInstanceProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoTicketRegistryProperties.class);
    private static final long serialVersionUID = 8243690796900311918L;

    /**
     * Whether collections should be dropped on startup and re-created.
     */
    private boolean dropCollection;

    public boolean isDropCollection() {
        return dropCollection;
    }

    public void setDropCollection(final boolean dropCollection) {
        this.dropCollection = dropCollection;
    }

}
