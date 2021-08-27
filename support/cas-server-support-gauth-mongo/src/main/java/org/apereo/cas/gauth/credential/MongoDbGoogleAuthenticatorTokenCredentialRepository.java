package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.ToString;
import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link MongoDbGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
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
    public OneTimeTokenAccount get(final long id) {
        val query = new Query();
        query.addCriteria(Criteria.where("id").is(id))
            .collation(Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.primary()));
        val r = this.mongoTemplate.findOne(query, GoogleAuthenticatorAccount.class, this.collectionName);
        return r != null ? decode(r) : null;
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        val query = new Query();
        query.addCriteria(Criteria.where("username").is(username.trim()).and("id").is(id))
            .collation(Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.primary()));
        val r = this.mongoTemplate.findOne(query, GoogleAuthenticatorAccount.class, this.collectionName);
        return r != null ? decode(r) : null;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        val query = new Query();
        query.addCriteria(Criteria.where("username").is(username.trim()))
            .collation(Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.primary()));
        val r = this.mongoTemplate.find(query, GoogleAuthenticatorAccount.class, this.collectionName);
        return decode(r);
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        val r = this.mongoTemplate.findAll(GoogleAuthenticatorAccount.class, this.collectionName);
        return r.stream()
            .map(this::decode)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        return update(account);
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
        query.addCriteria(Criteria.where("username").is(username.trim()))
            .collation(Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.primary()));
        this.mongoTemplate.remove(query, GoogleAuthenticatorAccount.class, this.collectionName);
    }

    @Override
    public void delete(final long id) {
        val query = new Query();
        query.addCriteria(Criteria.where("id").is(id))
            .collation(Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.primary()));
        this.mongoTemplate.remove(query, GoogleAuthenticatorAccount.class, this.collectionName);
    }

    @Override
    public long count() {
        val query = new Query();
        query.addCriteria(Criteria.where("username").exists(true));
        return this.mongoTemplate.count(query, GoogleAuthenticatorAccount.class, this.collectionName);
    }

    @Override
    public long count(final String username) {
        val query = new Query();
        query.addCriteria(Criteria.where("username").is(username.trim()))
            .collation(Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.primary()));
        return this.mongoTemplate.count(query, GoogleAuthenticatorAccount.class, this.collectionName);
    }
}
