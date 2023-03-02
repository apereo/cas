package org.apereo.cas.couchdb.core;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.impl.ObjectMapperFactory;

/**
 * This is {@link CouchDbConnectorFactory}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 * @deprecated Since 7
 */
@Deprecated(since = "7.0.0")
public interface CouchDbConnectorFactory {
    /**
     * Gets object mapper factory.
     *
     * @return the object mapper factory
     */
    ObjectMapperFactory getObjectMapperFactory();
    
    /**
     * Gets couch db connector.
     *
     * @return the couch db connector
     */
    CouchDbConnector getCouchDbConnector();

    /**
     * Create connector couch db.
     *
     * @return the couch db connector
     */
    CouchDbConnector createConnector();

    /**
     * Create couch db instance.
     *
     * @return the couch db instance
     */
    CouchDbInstance createInstance();

    /**
     * Gets couch db instance.
     *
     * @return the couch db instance
     */
    CouchDbInstance getCouchDbInstance();

}
