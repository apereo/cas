package org.apereo.cas.couchdb.gauth.token;

import lombok.val;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.UpdateHandler;
import org.ektorp.support.View;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link GoogleAuthenticatorTokenCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@View(name = "all", map = "function(doc) { if(doc.token && doc.userId) { emit(doc._id, doc) } }")
public class GoogleAuthenticatorTokenCouchDbRepository extends CouchDbRepositorySupport<CouchDbGoogleAuthenticatorToken> {
    public GoogleAuthenticatorTokenCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbGoogleAuthenticatorToken.class, db, createIfNotExists);
    }

    /**
     * Find first by uid, otp pair.
     *
     * @param uid uid to search
     * @param otp otp to search
     * @return token for uid, otp pair
     */
    @View(name = "by_uid_otp", map = "function(doc) { if(doc.token && doc.userId) { emit([doc.userId, doc.token], doc) } }")
    public CouchDbGoogleAuthenticatorToken findOneByUidForOtp(final String uid, final Integer otp) {
        val view = createQuery("by_uid_otp").key(ComplexKey.of(uid, otp)).limit(1);
        return db.queryView(view, CouchDbGoogleAuthenticatorToken.class).stream().findFirst().orElse(null);
    }

    /**
     * Find by issued date.
     *
     * @param localDateTime time to search for tokens before
     * @return tokens issued before given date
     */
    @View(name = "by_issued_date_time", map = "function(doc) { if(doc.token && doc.userId) { emit(doc.issuedDateTime, doc) } }")
    public Collection<CouchDbGoogleAuthenticatorToken> findByIssuedDateTimeBefore(final LocalDateTime localDateTime) {
        val view = createQuery("by_issued_date_time").endKey(localDateTime);
        return db.queryView(view, CouchDbGoogleAuthenticatorToken.class);
    }

    /**
     * Find tokens by user id.
     *
     * @param userId user id to search for
     * @return tokens belonging to use id
     */
    @View(name = "by_userId", map = "function(doc) { if(doc.token && doc.userId) { emit(doc.userId, doc) } }")
    public List<CouchDbGoogleAuthenticatorToken> findByUserId(final String userId) {
        return queryView("by_userId", userId);
    }

    /**
     * Token count for a user.
     *
     * @param userId user to count tokens for
     * @return count of the user's tokens
     */
    @View(name = "count_by_userId", map = "function(doc) { if(doc.token && doc.userId) { emit(doc.userId, doc) } }", reduce = "_count")
    public long countByUserId(final String userId) {
        val view = createQuery("count_by_userId").key(userId);
        val rows = db.queryView(view).getRows();
        if (rows.isEmpty()) {
            return 0;
        }
        return rows.get(0).getValueAsInt();
    }

    /**
     * Total number of tokens stored.
     *
     * @return number of tokens in database
     */
    @View(name = "count", map = "function(doc) { if(doc.token && doc.userId) { emit(doc._id, doc) } }", reduce = "_count")
    public long count() {
        val rows = db.queryView(createQuery("count")).getRows();
        if (rows.isEmpty()) {
            return 0;
        }
        return rows.get(0).getValueAsInt();
    }

    /**
     * Delete record, ignoring rev.
     *
     * @param token token to delete
     */
    @UpdateHandler(name = "delete_token", file = "CouchDbOneTimeToken_delete.js")
    public void deleteToken(final CouchDbGoogleAuthenticatorToken token) {
        db.callUpdateHandler(stdDesignDocumentId, "delete_token", token.getCid(), null);
    }


    /**
     * Find all by uid, otp pair.
     *
     * @param uid uid to search
     * @param otp otp to search
     * @return token for uid, otp pair
     */
    @View(name = "by_uid_otp", map = "function(doc) { if(doc.token && doc.userId) { emit([doc.userId, doc.token], doc) } }")
    public List<CouchDbGoogleAuthenticatorToken> findByUidForOtp(final String uid, final Integer otp) {
        val view = createQuery("by_uid_otp").key(ComplexKey.of(uid, otp));
        return db.queryView(view, CouchDbGoogleAuthenticatorToken.class);
    }

    /**
     * Find by token.
     *
     * @param otp token to search for
     * @return token for the otp
     */
    @View(name = "by_token", map = "function(doc) { if(doc.token && doc.userId) { emit(doc.token, doc) } }")
    public List<CouchDbGoogleAuthenticatorToken> findByToken(final Integer otp) {
        return queryView("by_token", otp);
    }
}
