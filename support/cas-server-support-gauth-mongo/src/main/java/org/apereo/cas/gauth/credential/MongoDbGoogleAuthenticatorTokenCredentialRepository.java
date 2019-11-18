package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link MongoDbGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString
@Getter
public class MongoDbGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorTokenCredentialRepository {
    private final MongoOperations mongoTemplate;
    private final String collectionName;

    public MongoDbGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                               final MongoOperations mongoTemplate,
                                                               final String collectionName,
                                                               final CipherExecutor<String, String> tokenCredentialCipher) {
        super(tokenCredentialCipher, googleAuthenticator);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public OneTimeTokenAccount get(final String username) {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("username").is(username));
            val r = this.mongoTemplate.findOne(query, GoogleAuthenticatorAccount.class, this.collectionName);
            if (r != null) {
                return decode(r);
            }
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", username);
        }
        return null;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        try {
            val r = this.mongoTemplate.findAll(GoogleAuthenticatorAccount.class, this.collectionName);
            return r.stream()
                .map(this::decode)
                .collect(Collectors.toList());

        } catch (final Exception e) {
            LOGGER.error("No record could be found for google authenticator", e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public void save(final String userName, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        val account = new GoogleAuthenticatorAccount(userName, secretKey, validationCode, scratchCodes);
        update(account);
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val encodedAccount = encode(account);
        this.mongoTemplate.save(encodedAccount, this.collectionName);
        return encodedAccount;
    }

    @Override
    public void deleteAll() {
        this.mongoTemplate.remove(new Query(), GoogleAuthenticatorAccount.class, this.collectionName);
    }

    @Override
    public void delete(final String username) {
        val query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        this.mongoTemplate.remove(query, GoogleAuthenticatorAccount.class, this.collectionName);
    }

    @Override
    public long count() {
        val query = new Query();
        query.addCriteria(Criteria.where("username").exists(true));
        return this.mongoTemplate.count(query, GoogleAuthenticatorAccount.class, this.collectionName);
    }
}
