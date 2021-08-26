package org.apereo.cas.couchdb.yubikey;

import lombok.val;
import org.ektorp.BulkDeleteDocument;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import java.util.stream.Collectors;

/**
 * This is {@link YubiKeyAccountCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@View(name = "all", map = "function(doc) { if(doc.devices && doc.username) {emit(doc._id, doc)} }")
public class YubiKeyAccountCouchDbRepository extends CouchDbRepositorySupport<CouchDbYubiKeyAccount> {
    public YubiKeyAccountCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbYubiKeyAccount.class, db, createIfNotExists);
    }

    /**
     * Find by username.
     *
     * @param uid username to search for
     * @return yubikey account for username provided
     */
    @View(name = "by_username", map = "function(doc) { if(doc.devices && doc.username) {emit(doc.username, doc)}}")
    public CouchDbYubiKeyAccount findByUsername(final String uid) {
        val view = createQuery("by_username").key(uid).limit(1).includeDocs(true);
        return db.queryView(view, CouchDbYubiKeyAccount.class).stream().findFirst().orElse(null);
    }

    /**
     * Remove.
     *
     * @param username the username
     * @param deviceId the id
     */
    public void remove(final String username, final long deviceId) {
        val account = findByUsername(username);
        if (account != null && account.getDevices().removeIf(device -> deviceId == device.getId())) {
            update(account);
        }
    }

    /**
     * Remove all and return acount.
     *
     * @return the count
     */
    public long removeAll() {
        return db.executeBulk(getAll().stream()
            .map(BulkDeleteDocument::of)
            .collect(Collectors.toList()))
            .size();
    }
}
