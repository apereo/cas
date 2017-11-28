package org.apereo.cas.adaptors.gauth;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apereo.cas.adaptors.gauth.repository.credentials.GoogleAuthenticatorAccount;
import org.apereo.cas.otp.repository.credentials.BaseOneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.persistence.NoResultException;
import java.util.List;

/**
 * This is {@link GoogleAuthenticatorMongoDbTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorMongoDbTokenCredentialRepository extends BaseOneTimeTokenCredentialRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAuthenticatorMongoDbTokenCredentialRepository.class);

    private final IGoogleAuthenticator googleAuthenticator;

    private final String collectionName;
    private final MongoOperations mongoTemplate;

    public GoogleAuthenticatorMongoDbTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                               final MongoOperations mongoTemplate,
                                                               final String collectionName) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
        this.googleAuthenticator = googleAuthenticator;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public OneTimeTokenAccount get(final String username) {
        try {
            final Query query = new Query();
            query.addCriteria(Criteria.where("username").is(username));
            final GoogleAuthenticatorAccount r = this.mongoTemplate.findOne(query, GoogleAuthenticatorAccount.class, this.collectionName);
            return r;
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", username);
        }
        return null;
    }

    @Override
    public void save(final String userName, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        final GoogleAuthenticatorAccount account = new GoogleAuthenticatorAccount(userName, secretKey, validationCode, scratchCodes);
        this.mongoTemplate.save(account, this.collectionName);
    }

    @Override
    public OneTimeTokenAccount create(final String username) {
        final GoogleAuthenticatorKey key = this.googleAuthenticator.createCredentials();
        return new GoogleAuthenticatorAccount(username, key.getKey(), key.getVerificationCode(), key.getScratchCodes());
    }
}
