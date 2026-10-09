package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * This is {@link MongoDbGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString
@Getter
@Slf4j
public class MongoDbGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorTokenCredentialRepository {
    /**
     * Name of the index on usernames. It carries the username collation, since a query with a collation can only
     * use an index with the same collation, and it is named so it does not clash with a plain index on usernames.
     */
    public static final String USERNAME_INDEX_NAME = "username_collated";

    /**
     * Usernames are matched ignoring case but not accents. Primary strength would also ignore accents, so
     * {@code jose} and {@code josé} would share devices. Case is still ignored because documents written before
     * usernames were stored lowercased may hold mixed-case names.
     */
    private static final Collation USERNAME_COLLATION = Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.secondary());


    private final MongoOperations mongoTemplate;

    private final String collectionName;

    public MongoDbGoogleAuthenticatorTokenCredentialRepository(
        final CasGoogleAuthenticator googleAuthenticator,
        final MongoOperations mongoTemplate,
        final String collectionName,
        final CipherExecutor<String, String> tokenCredentialCipher,
        final CipherExecutor<Number, Number> scratchCodesCipher) {
        super(tokenCredentialCipher, scratchCodesCipher, googleAuthenticator);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    /**
     * Create the index that lookups by username use. Creating an index that already exists with the same options does
     * nothing. A failure, such as missing privileges, is logged and does not stop startup; lookups then scan the collection.
     * Lookups by id use the {@code _id} index and carry no collation, so they need nothing else.
     */
    public void createIndexes() {
        val index = new Index()
            .on("username", Sort.Direction.ASC)
            .named(USERNAME_INDEX_NAME)
            .collation(USERNAME_COLLATION);
        try {
            mongoTemplate.indexOps(collectionName).createIndex(index);
        } catch (final Exception e) {
            LOGGER.warn("Unable to create index [{}] on collection [{}]: [{}]", USERNAME_INDEX_NAME, collectionName, e.getMessage());
        }
    }

    @Override
    public OneTimeTokenAccount get(final long id) {
        val query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        val r = this.mongoTemplate.findOne(query, GoogleAuthenticatorAccount.class, this.collectionName);
        return Optional.ofNullable(r).map(this::decode).orElse(null);
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        val query = new Query();
        query.addCriteria(Criteria.where("username").is(username.trim()).and("id").is(id))
            .collation(USERNAME_COLLATION);
        val r = this.mongoTemplate.findOne(query, GoogleAuthenticatorAccount.class, this.collectionName);
        return Optional.ofNullable(r).map(this::decode).orElse(null);
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        val query = new Query();
        query.addCriteria(Criteria.where("username").is(username.trim()))
            .collation(USERNAME_COLLATION);
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
        return update(account.assignIdIfNecessary());
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
            .collation(USERNAME_COLLATION);
        this.mongoTemplate.remove(query, GoogleAuthenticatorAccount.class, this.collectionName);
    }

    @Override
    public void delete(final long id) {
        val query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
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
            .collation(USERNAME_COLLATION);
        return this.mongoTemplate.count(query, GoogleAuthenticatorAccount.class, this.collectionName);
    }
}
