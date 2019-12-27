package org.apereo.cas.couchdb.yubikey;

import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

/**
 * This is {@link YubiKeyAccountCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@View(name = "all", map = "function(doc) { if(doc.deviceIdentifiers && doc.username) {emit(doc._id, doc)} }")
public class YubiKeyAccountCouchDbRepository extends CouchDbRepositorySupport<CouchDbYubiKeyAccount> {
    public YubiKeyAccountCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbYubiKeyAccount.class, db, createIfNotExists);
    }

    /**
     * Find by username.
     * @param uid username to search for
     * @return yubikey account for username provided
     */
    @View(name = "by_username", map = "function(doc) { if(doc.deviceIdentifiers && doc.username) {emit(doc.username, doc)}}")
    public CouchDbYubiKeyAccount findByUsername(final String uid) {
        val view = createQuery("by_username").key(uid).limit(1).includeDocs(true);
        return db.queryView(view, CouchDbYubiKeyAccount.class).stream().findFirst().orElse(null);
    }
}
