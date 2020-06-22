package org.apereo.cas.couchdb.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.UpdateHandler;
import org.ektorp.support.View;

import java.util.ArrayList;
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
     *
     * @param username username for lookup
     * @return first one time token account for user
     */
    @View(name = "by_username", map = "function(doc) { if(doc.secretKey) { emit(doc.username, doc) } }")
    public List<CouchDbGoogleAuthenticatorAccount> findByUsername(final String username) {
        try {
            return queryView("by_username", username);
        } catch (final DocumentNotFoundException e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Delete token without revision checks.
     *
     * @param token token to delete
     */
    @UpdateHandler(name = "delete_token_account", file = "CouchDbOneTimeTokenAccount_delete.js")
    public void deleteTokenAccount(final CouchDbGoogleAuthenticatorAccount token) {
        db.callUpdateHandler(stdDesignDocumentId, "delete_token_account", token.getCid(), null);
    }

    /**
     * Total token accounts in database.
     *
     * @return count of accounts
     */
    @View(name = "count", map = "function(doc) { if(doc.secretKey) { emit(doc._id, doc) } }", reduce = "_count")
    public long count() {
        val rows = db.queryView(createQuery("count")).getRows();
        return rows.isEmpty() ? 0 : rows.get(0).getValueAsInt();
    }

    /**
     * Count devices per user.
     *
     * @param username the username
     * @return the count
     */
    @View(name = "count_by_username", map = "function(doc) { if(doc.secretKey) { emit(doc.username, doc._id, doc) } }", reduce = "_count")
    public long count(final String username) {
        val rows = db.queryView(createQuery("count_by_username").key(username)).getRows();
        return rows.isEmpty() ? 0 : rows.get(0).getValueAsInt();
    }
    
    /**
     * Find by id one time token account.
     *
     * @param id the id
     * @return the one time token account
     */
    @View(name = "by_id", map = "function(doc) { if(doc.secretKey) { emit(doc.id, doc) } }")
    public OneTimeTokenAccount findById(final long id) {
        val view = createQuery("by_id").key(id).limit(1);
        return db.queryView(view, CouchDbGoogleAuthenticatorAccount.class).stream().findFirst().orElse(null);
    }

    /**
     * Find by id and username one time token account.
     *
     * @param id       the id
     * @param username the username
     * @return the one time token account
     */
    @View(name = "by_id_username", map = "function(doc) { emit([doc.id, doc.username], doc) }")
    public OneTimeTokenAccount findByIdAndUsername(final long id, final String username) {
        try {
            val view = createQuery("by_id_username").key(ComplexKey.of(id, username)).limit(1);
            return db.queryView(view, CouchDbGoogleAuthenticatorAccount.class).stream().findFirst().orElse(null);
        } catch (final DocumentNotFoundException e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return null;
    }


}
