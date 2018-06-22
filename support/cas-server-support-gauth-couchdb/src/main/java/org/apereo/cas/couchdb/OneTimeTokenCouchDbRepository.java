package org.apereo.cas.couchdb;

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
 * This is {@link OneTimeTokenCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@View(name = "all", map = "function(doc) { if(doc.token && doc.userId) { emit(doc._id, doc) } }")
public class OneTimeTokenCouchDbRepository extends CouchDbRepositorySupport<CouchDbOneTimeToken> {
    public OneTimeTokenCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbOneTimeToken.class, db, createIfNotExists);
    }

    @View(name = "by_uid_otp", map = "function(doc) { if(doc.token && doc.userId) { emit([doc.userId, doc.token], doc) } }")
    public CouchDbOneTimeToken findOneByUidForOtp(final String uid, final Integer otp) {
        val view = createQuery("by_uid_otp").key(ComplexKey.of(uid, otp)).limit(1);
        return db.queryView(view, CouchDbOneTimeToken.class).stream().findFirst().orElse(null);
    }

    @View(name = "by_issued_date_time", map = "function(doc) { if(doc.token && doc.userId) { emit(doc.issuedDateTime, doc) } }")
    public Collection<CouchDbOneTimeToken> findByIssuedDateTimeBefore(final LocalDateTime localDateTime) {
        val view = createQuery("by_issued_date_time").endKey(localDateTime);
        return db.queryView(view, CouchDbOneTimeToken.class);
    }

    @View(name = "by_userId", map = "function(doc) { if(doc.token && doc.userId) { emit(doc.userId, doc) } }")
    public List<CouchDbOneTimeToken> findByUserId(final String userId) {
        return queryView("by_userId", userId);
    }

    @View(name = "count_by_userId", map = "function(doc) { if(doc.token && doc.userId) { emit(doc.userId, doc) } }", reduce = "_count")
    public long countByUserId(final String userId) {
        val view = createQuery("count_by_userId").key(userId);
        return db.queryView(view).getRows().get(0).getValueAsInt();
    }

    @View(name = "count", map = "function(doc) { if(doc.token && doc.userId) { emit(doc._id, doc) } }", reduce = "_count")
    public long count() {
        return db.queryView(createQuery("count")).getRows().get(0).getValueAsInt();
    }

    @UpdateHandler(name = "delete_token", file = "CouchDbOneTimeTokenAccount_delete.js")
    public void deleteToken(final CouchDbOneTimeToken token) {
        db.callUpdateHandler(stdDesignDocumentId, "delete_token", token.getCid(), null);
    }

    public List<CouchDbOneTimeToken> findByUidForOtp(final String uid, final Integer otp) {
        return queryView("by_uid_otp", ComplexKey.of());
    }

    @View(name = "by_token", map = "function(doc) { if(doc.token && doc.userId) { emit(doc.token, doc) } }")
    public List<CouchDbOneTimeToken> findByToken(final Integer otp) {
        return queryView("by_token", otp);
    }
}
