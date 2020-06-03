package org.apereo.cas.couchdb.u2f;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.GenerateView;
import org.ektorp.support.UpdateHandler;
import org.ektorp.support.View;

import java.time.LocalDate;
import java.util.List;

/**
 * This is {@link U2FDeviceRegistrationCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class U2FDeviceRegistrationCouchDbRepository extends CouchDbRepositorySupport<CouchDbU2FDeviceRegistration> {
    private final CouchDbInstance couchDbInstance;

    public U2FDeviceRegistrationCouchDbRepository(final CouchDbConnector couchDbConnector,
                                                  final CouchDbInstance couchDbInstance,
                                                  final boolean createIfNotExists) {
        super(CouchDbU2FDeviceRegistration.class, couchDbConnector, createIfNotExists);
        this.couchDbInstance = couchDbInstance;
    }

    /**
     * Find by username.
     *
     * @param username name to search for
     * @return registrations for user
     */
    @GenerateView
    public List<CouchDbU2FDeviceRegistration> findByUsername(final String username) {
        return queryView("by_username", username);
    }

    /**
     * Find expired records.
     *
     * @param expirationDate date to search until
     * @return expired records
     */
    @View(name = "by_createdDate", map = "function(doc) { if(doc.record && doc.createdDate && doc.username) { emit(doc.createdDate, doc) } }")
    public List<CouchDbU2FDeviceRegistration> findByDateBefore(final LocalDate expirationDate) {
        return db.queryView(createQuery("by_createdDate").endKey(expirationDate), CouchDbU2FDeviceRegistration.class);
    }

    /**
     * Delete a record without revision checks.
     *
     * @param record record to be deleted
     */
    @UpdateHandler(name = "delete_record", file = "delete.js")
    public void deleteRecord(final CouchDbU2FDeviceRegistration record) {
        db.callUpdateHandler(stdDesignDocumentId, "delete_record", record.getCid(), null);
    }

    /**
     * Delete all records without revision checks.
     */
    public void deleteAll() {
        this.couchDbInstance.deleteDatabase(db.getDatabaseName());
        this.couchDbInstance.createDatabaseIfNotExists(db.getDatabaseName());
        initStandardDesignDocument();
    }
}
