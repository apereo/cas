package org.apereo.cas.adaptors.gauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.persistence.NoResultException;
import java.util.List;

/**
 * This is {@link MongoDbGoogleAuthenticatorAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MongoDbGoogleAuthenticatorAccountRegistry extends BaseGoogleAuthenticatorCredentialRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbGoogleAuthenticatorAccountRegistry.class);

    private String collectionName;

    private boolean dropCollection;

    private MongoOperations mongoTemplate;

    public MongoDbGoogleAuthenticatorAccountRegistry() {
    }

    public MongoDbGoogleAuthenticatorAccountRegistry(final MongoOperations mongoTemplate,
                                                     final String collectionName,
                                                     final boolean dropCollection) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
        this.dropCollection = dropCollection;
    }

    /**
     * Initialized registry post construction.
     * Will decide if the configured collection should
     * be dropped and recreated.
     */
    @PostConstruct
    public void init() {
        Assert.notNull(this.mongoTemplate);

        if (this.dropCollection) {
            LOGGER.debug("Dropping database collection: {}", this.collectionName);
            this.mongoTemplate.dropCollection(this.collectionName);
        }

        if (!this.mongoTemplate.collectionExists(this.collectionName)) {
            LOGGER.debug("Creating database collection: {}", this.collectionName);
            this.mongoTemplate.createCollection(this.collectionName);
        }
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(final String collectionName) {
        this.collectionName = collectionName;
    }

    public boolean isDropCollection() {
        return dropCollection;
    }

    public void setDropCollection(final boolean dropCollection) {
        this.dropCollection = dropCollection;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public String getSecretKey(final String username) {
        try {
            final Query query = new Query();
            query.addCriteria(Criteria.where("userName").is(username));
            final MongoDbGoogleAuthenticatorRecord r = 
                    this.mongoTemplate.findOne(query, MongoDbGoogleAuthenticatorRecord.class, this.collectionName);
            
            if (r != null) {
                return r.getSecretKey();
            }
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id {}", username);
        }
        return null;
    }

    @Override
    public void saveUserCredentials(final String userName, final String secretKey,
                                    final int validationCode,
                                    final List<Integer> scratchCodes) {
        final MongoDbGoogleAuthenticatorRecord r = new MongoDbGoogleAuthenticatorRecord();
        r.setScratchCodes(scratchCodes);
        r.setSecretKey(secretKey);
        r.setUserName(userName);
        r.setValidationCode(validationCode);
        this.mongoTemplate.save(r, this.collectionName);
    }
}
