package org.apereo.cas.couchdb.gauth.credential;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.UpdateHandler;
import org.ektorp.support.View;

import java.util.List;

/**
 * This is {@link GoogleAuthenticatorAccountCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@View(name = "all", map = "function(doc) { if(doc.secretKey) { emit(doc._id, doc) } }")
@Slf4j
public class GoogleAuthenticatorAccountCouchDbRepository extends CouchDbRepositorySupport<CouchDbGoogleAuthenticatorAccount> {

    public GoogleAuthenticatorAccountCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbGoogleAuthenticatorAccount.class, db, createIfNotExists);
    }

    /**
     * Find first account for user.
     * @param username username for lookup
     * @return first one time token account for user
     */
    @View(name = "by_username", map = "function(doc) { if(doc.secretKey) { emit(doc.username, doc) } }")
    public CouchDbGoogleAuthenticatorAccount findOneByUsername(final String username) {
        val view = createQuery("by_username").key(username).limit(1);
        try {
            return db.queryView(view, CouchDbGoogleAuthenticatorAccount.class).stream().findFirst().orElse(null);
        } catch (final DocumentNotFoundException ignored) {
            return null;
        }
    }

    /**
     * Final all accounts for user.
     * @param username username for account lookup
     * @return one time token accounts for user
     */
    public List<CouchDbGoogleAuthenticatorAccount> findByUsername(final String username) {
        try {
            return queryView("by_username", username);
        } catch (final DocumentNotFoundException e) {
            LOGGER.trace(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Delete token without revision checks.
     * @param token token to delete
     */
    @UpdateHandler(name = "delete_token_account", file = "CouchDbOneTimeTokenAccount_delete.js")
    public void deleteTokenAccount(final CouchDbGoogleAuthenticatorAccount token) {
        db.callUpdateHandler(stdDesignDocumentId, "delete_token_account", token.getCid(), null);
    }

    /**
     * Total token accounts in database.
     * @return count of accounts
     */
    @View(name = "count", map = "function(doc) { if(doc.secretKey) { emit(doc._id, doc) } }", reduce = "_count")
    public long count() {
        val rows = db.queryView(createQuery("count")).getRows();
        return rows.isEmpty() ? 0 : rows.get(0).getValueAsInt();
    }
}
