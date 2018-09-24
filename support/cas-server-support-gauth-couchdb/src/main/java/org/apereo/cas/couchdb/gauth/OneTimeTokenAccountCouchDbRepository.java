package org.apereo.cas.couchdb.gauth;

import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.UpdateHandler;
import org.ektorp.support.View;

import java.util.List;

/**
 * This is {@link OneTimeTokenAccountCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@View(name = "all", map = "function(doc) { if(doc.secretKey) { emit(doc._id, doc) } }")
public class OneTimeTokenAccountCouchDbRepository extends CouchDbRepositorySupport<CouchDbOneTimeTokenAccount> {

    public OneTimeTokenAccountCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbOneTimeTokenAccount.class, db, createIfNotExists);
    }

    /**
     * Find first account for user.
     * @param username username for lookup
     * @return first one time token account for user
     */
    @View(name = "by_username", map = "function(doc) { if(doc.secretKey) { emit(doc.username, doc) } }")
    public CouchDbOneTimeTokenAccount findOneByUsername(final String username) {
        val view = createQuery("by_username").key(username).limit(1);
        return db.queryView(view, CouchDbOneTimeTokenAccount.class).stream().findFirst().orElse(null);
    }

    /**
     * Final all accounts for user.
     * @param username username for account lookup
     * @return one time token accounts for user
     */
    public List<CouchDbOneTimeTokenAccount> findByUsername(final String username) {
        return queryView("by_username", username);
    }

    /**
     * Delete token without revision checks.
     * @param token token to delete
     */
    @UpdateHandler(name = "delete_token_account", file = "CouchDbOneTimeTokenAccount_delete.js")
    public void deleteTokenAccount(final CouchDbOneTimeTokenAccount token) {
        db.callUpdateHandler(stdDesignDocumentId, "delete_token_account", token.getCid(), null);
    }

    /**
     * Total token accounts in database.
     * @return count of accounts
     */
    @View(name = "count", map = "function(doc) { if(doc.secretKey) { emit(doc._id, doc) } }", reduce = "_count")
    public long count() {
        return db.queryView(createQuery("count")).getRows().get(0).getValueAsInt();
    }
}
