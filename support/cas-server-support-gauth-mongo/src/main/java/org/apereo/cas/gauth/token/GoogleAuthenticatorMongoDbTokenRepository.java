package org.apereo.cas.gauth.token;

import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.persistence.NoResultException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * This is {@link GoogleAuthenticatorMongoDbTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthenticatorMongoDbTokenRepository extends BaseOneTimeTokenRepository {
    private final MongoOperations mongoTemplate;
    private final String collectionName;
    private final long expireTokensInSeconds;

    @Override
    public void store(final OneTimeToken token) {
        this.mongoTemplate.save(token, this.collectionName);
    }

    @Override
    public GoogleAuthenticatorToken get(final String uid, final Integer otp) {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("userId").is(uid).and("token").is(otp));
            return this.mongoTemplate.findOne(query, GoogleAuthenticatorToken.class, this.collectionName);
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", uid);
        }
        return null;
    }

    @Override
    public void removeAll() {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("userId").exists(true));
            this.mongoTemplate.remove(query, GoogleAuthenticatorToken.class, this.collectionName);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    protected void cleanInternal() {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("issuedDateTime").gte(LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(this.expireTokensInSeconds)));
            this.mongoTemplate.remove(query, GoogleAuthenticatorToken.class, this.collectionName);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public void remove(final String uid, final Integer otp) {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("userId").is(uid).and("token").is(otp));
            this.mongoTemplate.remove(query, GoogleAuthenticatorToken.class, this.collectionName);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public void remove(final String uid) {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("userId").is(uid));
            this.mongoTemplate.remove(query, GoogleAuthenticatorToken.class, this.collectionName);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public void remove(final Integer otp) {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("token").is(otp));
            this.mongoTemplate.remove(query, GoogleAuthenticatorToken.class, this.collectionName);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public long count(final String uid) {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("userId").is(uid));
            return this.mongoTemplate.count(query, GoogleAuthenticatorToken.class, this.collectionName);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public long count() {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("userId").exists(true));
            return this.mongoTemplate.count(query, GoogleAuthenticatorToken.class, this.collectionName);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return 0;
    }
}
