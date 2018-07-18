package org.apereo.cas.couchdb.services;

import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

/**
 * This is {@link RegisteredServiceRepository}. Typed interface to CouchDB.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@View(name = "all", map = "function(doc) { emit(null, doc._id) }")
public class RegisteredServiceRepository extends CouchDbRepositorySupport<RegisteredServiceDocument> {
    public RegisteredServiceRepository(final CouchDbConnector db) {
        this(db, true);
    }

    public RegisteredServiceRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(RegisteredServiceDocument.class, db, createIfNotExists);
    }

    /**
     * Implements search by serviceId.
     *
     * @param serviceId The serviceId of the service to find.
     * @return The service found or +null+.
     */
    @View(name = "by_serviceId", map = "function(doc) { emit(doc.service.serviceId, doc._id) }")
    public RegisteredServiceDocument findByServiceId(final String serviceId) {
        return queryView("by_serviceId", serviceId).stream().findFirst().orElse(null);
    }

    /**
     * Implements search by service name.
     *
     * @param serviceName The service name of the service to find.
     * @return The service found or +null+.
     */
    @View(name = "by_serviceName", map = "function(doc) { emit(doc.service.name, doc._id) }")
    public RegisteredServiceDocument findByServiceName(final String serviceName) {
        return queryView("by_serviceName", serviceName).stream().findFirst().orElse(null);
    }

    /**
     * Overload wrapper for long type. Get service by ID.
     *
     * @param id Service ID
     * @return service
     */
    public RegisteredServiceDocument get(final long id) {
        return this.get(String.valueOf(id));
    }

    /**
     * Size of the service database.
     *
     * @return The service count in the database.
     */
    @View(name = "size", map = "function(doc) {emit(null, doc._id)}", reduce = "function(keys, values, combine) {return values.length}")
    public int size() {
        val r = db.queryView(createQuery("size"));
        return r.getRows().get(0).getValueAsInt();
    }

}
