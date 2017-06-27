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

    @Override
    public void setCollectionName(final String collectionName) {
        LOGGER.warn("Cannot set collection name for MongoDb Ticket Registry. "
                + "Collection names for tickets are dynamically determined by the ticket catalog");
    }

    @Override
    public String getCollectionName() {
        LOGGER.warn("Cannot retrieve collection name for MongoDb Ticket Registry. "
                + "Collection names for tickets are dynamically determined by the ticket catalog");
        return null;
    }
}
