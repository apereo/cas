package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.adaptors.gauth.repository.token.GoogleAuthenticatorToken;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepository;
import org.apereo.cas.otp.repository.token.OneTimeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import javax.persistence.NoResultException;
import java.time.LocalDateTime;

/**
 * This is {@link MongoDbGoogleAuthenticatorTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class MongoDbGoogleAuthenticatorTokenRepository extends BaseOneTimeTokenRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbGoogleAuthenticatorTokenRepository.class);
    
    private final long expireTokensInSeconds;
    private final String collectionName;
    private final MongoOperations mongoTemplate;

    public MongoDbGoogleAuthenticatorTokenRepository(final MongoOperations mongoTemplate,
                                                     final String collectionName,
                                                     final boolean dropCollection,
                                                     final long expireTokensInSeconds) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
        this.expireTokensInSeconds = expireTokensInSeconds;

        Assert.notNull(this.mongoTemplate);
        Assert.notNull(this.collectionName);

        if (dropCollection) {
            LOGGER.debug("Dropping database collection: [{}]", this.collectionName);
            this.mongoTemplate.dropCollection(this.collectionName);
        }

        if (!this.mongoTemplate.collectionExists(this.collectionName)) {
            LOGGER.debug("Creating database collection: [{}]", this.collectionName);
            this.mongoTemplate.createCollection(this.collectionName);
        }
    }

    @Override
    public void store(final OneTimeToken token) {
        this.mongoTemplate.save(token, this.collectionName);
    }

    @Override
    public boolean exists(final String uid, final Integer otp) {
        try {
            final Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(uid).and("token").is(otp));
            final GoogleAuthenticatorToken r = this.mongoTemplate.findOne(query, GoogleAuthenticatorToken.class, this.collectionName);
            return r != null;
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", uid);
        }
        return false;
    }

    @Override
    protected void cleanInternal() {
        try {
            final Query query = new Query();
            query.addCriteria(Criteria.where("issuedDateTime").gte(LocalDateTime.now().minusSeconds(this.expireTokensInSeconds)));
            this.mongoTemplate.remove(query, GoogleAuthenticatorToken.class, this.collectionName);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
