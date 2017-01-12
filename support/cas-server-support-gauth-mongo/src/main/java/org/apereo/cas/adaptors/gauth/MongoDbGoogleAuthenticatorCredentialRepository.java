package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.adaptors.gauth.repository.credentials.BaseGoogleAuthenticatorCredentialRepository;
import org.apereo.cas.adaptors.gauth.repository.credentials.GoogleAuthenticatorAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import javax.persistence.NoResultException;
import java.util.List;

/**
 * This is {@link MongoDbGoogleAuthenticatorCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MongoDbGoogleAuthenticatorCredentialRepository extends BaseGoogleAuthenticatorCredentialRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbGoogleAuthenticatorCredentialRepository.class);

    private final String collectionName;
    private final MongoOperations mongoTemplate;

    public MongoDbGoogleAuthenticatorCredentialRepository(final MongoOperations mongoTemplate, final String collectionName, final boolean dropCollection) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;

        Assert.notNull(this.mongoTemplate);

        if (dropCollection) {
            LOGGER.debug("Dropping database collection: {}", this.collectionName);
            this.mongoTemplate.dropCollection(this.collectionName);
        }

        if (!this.mongoTemplate.collectionExists(this.collectionName)) {
            LOGGER.debug("Creating database collection: {}", this.collectionName);
            this.mongoTemplate.createCollection(this.collectionName);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public String getSecretKey(final String username) {
        try {
            final Query query = new Query();
            query.addCriteria(Criteria.where("username").is(username));
            final GoogleAuthenticatorAccount r = this.mongoTemplate.findOne(query, GoogleAuthenticatorAccount.class, this.collectionName);

            if (r != null) {
                return r.getSecretKey();
            }
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id {}", username);
        }
        return null;
    }

    @Override
    public void saveUserCredentials(final String userName, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        final GoogleAuthenticatorAccount account = new GoogleAuthenticatorAccount(userName, secretKey, validationCode, scratchCodes);
        this.mongoTemplate.save(account, this.collectionName);
    }
}
