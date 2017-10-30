package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.adaptors.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepository;
import org.apereo.cas.otp.repository.token.OneTimeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.persistence.NoResultException;
import java.time.LocalDateTime;

/**
 * This is {@link GoogleAuthenticatorMongoDbTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GoogleAuthenticatorMongoDbTokenRepository extends BaseOneTimeTokenRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAuthenticatorMongoDbTokenRepository.class);

    private final long expireTokensInSeconds;
    private final String collectionName;
    private final MongoOperations mongoTemplate;

    public GoogleAuthenticatorMongoDbTokenRepository(final MongoOperations mongoTemplate,
                                                     final String collectionName,
                                                     final long expireTokensInSeconds) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
        this.expireTokensInSeconds = expireTokensInSeconds;
    }

    @Override
    public void store(final OneTimeToken token) {
        this.mongoTemplate.save(token, this.collectionName);
    }

    @Override
    public GoogleAuthenticatorToken get(final String uid, final Integer otp) {
        try {
            final Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(uid).and("token").is(otp));
            final GoogleAuthenticatorToken r = this.mongoTemplate.findOne(query, GoogleAuthenticatorToken.class, this.collectionName);
            return r;
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", uid);
        }
        return null;
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
