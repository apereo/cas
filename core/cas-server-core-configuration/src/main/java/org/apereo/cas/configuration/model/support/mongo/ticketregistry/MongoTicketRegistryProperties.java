package org.apereo.cas.configuration.model.support.mongo.ticketregistry;

import org.apereo.cas.configuration.model.support.mongo.AbstractMongoInstanceProperties;

/**
 * This is {@link MongoTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class MongoTicketRegistryProperties extends AbstractMongoInstanceProperties {
    public MongoTicketRegistryProperties() {
        setCollectionName("cas-ticket-registry");
    }
}
