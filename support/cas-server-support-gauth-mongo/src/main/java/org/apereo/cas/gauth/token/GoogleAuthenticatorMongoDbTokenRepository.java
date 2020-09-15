package org.apereo.cas.gauth.token;

import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * This is {@link GoogleAuthenticatorMongoDbTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
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
        val query = new Query();
        query.addCriteria(Criteria.where("userId").is(uid).and("token").is(otp));
        return this.mongoTemplate.findOne(query, GoogleAuthenticatorToken.class, this.collectionName);
    }

    @Override
    public void remove(final String uid, final Integer otp) {
        val query = new Query();
        query.addCriteria(Criteria.where("userId").is(uid).and("token").is(otp));
        this.mongoTemplate.remove(query, GoogleAuthenticatorToken.class, this.collectionName);
    }

    @Override
    public void remove(final String uid) {
        val query = new Query();
        query.addCriteria(Criteria.where("userId").is(uid));
        this.mongoTemplate.remove(query, GoogleAuthenticatorToken.class, this.collectionName);
    }

    @Override
    public void remove(final Integer otp) {
        val query = new Query();
        query.addCriteria(Criteria.where("token").is(otp));
        this.mongoTemplate.remove(query, GoogleAuthenticatorToken.class, this.collectionName);
    }

    @Override
    public void removeAll() {
        val query = new Query();
        query.addCriteria(Criteria.where("userId").exists(true));
        this.mongoTemplate.remove(query, GoogleAuthenticatorToken.class, this.collectionName);
    }

    @Override
    public long count(final String uid) {
        val query = new Query();
        query.addCriteria(Criteria.where("userId").is(uid));
        return this.mongoTemplate.count(query, GoogleAuthenticatorToken.class, this.collectionName);
    }

    @Override
    public long count() {
        val query = new Query();
        query.addCriteria(Criteria.where("userId").exists(true));
        return this.mongoTemplate.count(query, GoogleAuthenticatorToken.class, this.collectionName);
    }

    @Override
    protected void cleanInternal() {
        val query = new Query();
        query.addCriteria(Criteria.where("issuedDateTime")
            .gte(LocalDateTime.now(ZoneId.systemDefault())
                .minusSeconds(this.expireTokensInSeconds)));
        this.mongoTemplate.remove(query, GoogleAuthenticatorToken.class, this.collectionName);
    }
}
