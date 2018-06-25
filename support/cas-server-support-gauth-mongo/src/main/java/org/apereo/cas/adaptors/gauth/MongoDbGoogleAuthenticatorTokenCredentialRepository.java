package org.apereo.cas.adaptors.gauth;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.adaptors.gauth.repository.credentials.GoogleAuthenticatorAccount;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.BaseOneTimeTokenCredentialRepository;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.persistence.NoResultException;
import java.util.List;

/**
 * This is {@link MongoDbGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString
public class MongoDbGoogleAuthenticatorTokenCredentialRepository extends BaseOneTimeTokenCredentialRepository {

    private final IGoogleAuthenticator googleAuthenticator;
    private final MongoOperations mongoTemplate;
    private final String collectionName;

    public MongoDbGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                               final MongoOperations mongoTemplate, final String collectionName,
                                                               final CipherExecutor<String, String> tokenCredentialCipher) {
        super(tokenCredentialCipher);
        this.googleAuthenticator = googleAuthenticator;
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public OneTimeTokenAccount get(final String username) {
        try {
            final var query = new Query();
            query.addCriteria(Criteria.where("username").is(username));
            final var r = this.mongoTemplate.findOne(query, GoogleAuthenticatorAccount.class, this.collectionName);
            if (r != null) {
                return decode(r);
            }
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", username);
        }
        return null;
    }

    @Override
    public void save(final String userName, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        final var account = new GoogleAuthenticatorAccount(userName, secretKey, validationCode, scratchCodes);
        update(account);
    }

    @Override
    public OneTimeTokenAccount create(final String username) {
        final var key = this.googleAuthenticator.createCredentials();
        return new GoogleAuthenticatorAccount(username, key.getKey(), key.getVerificationCode(), key.getScratchCodes());
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        final var encodedAccount = encode(account);
        this.mongoTemplate.save(encodedAccount, this.collectionName);
        return encodedAccount;
    }

    @Override
    public void deleteAll() {
        this.mongoTemplate.remove(new Query(), GoogleAuthenticatorAccount.class, this.collectionName);
    }
}
