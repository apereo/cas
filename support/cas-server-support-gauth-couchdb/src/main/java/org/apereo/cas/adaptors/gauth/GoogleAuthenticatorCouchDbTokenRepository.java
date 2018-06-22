package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.couchdb.CouchDbOneTimeToken;
import org.apereo.cas.couchdb.OneTimeTokenCouchDbRepository;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ektorp.UpdateConflictException;

import java.time.LocalDateTime;

/**
 * This is {@link GoogleAuthenticatorCouchDbTokenRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Slf4j
@AllArgsConstructor
public class GoogleAuthenticatorCouchDbTokenRepository extends BaseOneTimeTokenRepository {

    private final OneTimeTokenCouchDbRepository couchDb;
    private final long expireTokensInSeconds;


    @Override
    public void store(final OneTimeToken token) {
        couchDb.add(new CouchDbOneTimeToken(token));
    }

    @Override
    public OneTimeToken get(final String uid, final Integer otp) {
        return couchDb.findOneByUidForOtp(uid, otp);
    }

    @Override
    public void remove(final String uid, final Integer otp) {
        couchDb.findByUidForOtp(uid, otp).forEach(couchDb::deleteToken);
    }

    @Override
    public void remove(final String uid) {
        couchDb.findByUserId(uid).forEach(couchDb::deleteToken);
    }

    @Override
    public void remove(final Integer otp) {
        couchDb.findByToken(otp).forEach(couchDb::deleteToken);
    }

    @Override
    public void removeAll() {
        couchDb.getAll().forEach(couchDb::deleteToken);
    }

    @Override
    public long count(final String uid) {
        return couchDb.countByUserId(uid);
    }

    @Override
    public long count() {
        return couchDb.count();
    }

    @Override
    protected void cleanInternal() {
        try {
            couchDb.findByIssuedDateTimeBefore(LocalDateTime.now().minusSeconds(expireTokensInSeconds)).forEach(couchDb::remove);
        } catch (final UpdateConflictException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
