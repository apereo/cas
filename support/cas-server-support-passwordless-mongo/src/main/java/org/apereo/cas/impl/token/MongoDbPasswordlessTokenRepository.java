package org.apereo.cas.impl.token;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.configuration.model.support.passwordless.token.PasswordlessAuthenticationMongoDbTokensProperties;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * This is {@link MongoDbPasswordlessTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class MongoDbPasswordlessTokenRepository extends BasePasswordlessTokenRepository {
    private final MongoOperations mongoTemplate;

    private final PasswordlessAuthenticationMongoDbTokensProperties properties;

    public MongoDbPasswordlessTokenRepository(final PasswordlessAuthenticationMongoDbTokensProperties properties,
                                              final long tokenExpirationInSeconds,
                                              final CipherExecutor cipherExecutor,
                                              final MongoOperations mongoTemplate) {
        super(tokenExpirationInSeconds, cipherExecutor);
        this.mongoTemplate = mongoTemplate;
        this.properties = properties;
    }

    @Override
    public Optional<PasswordlessAuthenticationToken> findToken(final String username) {
        val query = new Query().addCriteria(Criteria.where("username").is(hashUsername(username)));
        val authnToken = mongoTemplate.findOne(query, MongoDbPasswordlessAuthenticationEntity.class, properties.getCollection());
        return Optional.ofNullable(authnToken).map(token -> decodePasswordlessAuthenticationToken(token.getRecord()));
    }

    private static String hashUsername(final String username) {
        return DigestUtils.sha256(username);
    }

    @Override
    public void deleteTokens(final String username) {
        val query = new Query().addCriteria(Criteria.where("username").is(hashUsername(username)));
        mongoTemplate.remove(query, MongoDbPasswordlessAuthenticationEntity.class, properties.getCollection());
    }

    @Override
    public void deleteToken(final PasswordlessAuthenticationToken token) {
        val query = new Query().addCriteria(Criteria.where("username").is(hashUsername(token.getUsername())).and("id").is(token.getId()));
        val result = mongoTemplate.remove(query, MongoDbPasswordlessAuthenticationEntity.class, properties.getCollection());
        LOGGER.debug("Removed [{}] token record(s)", result.getDeletedCount());
    }

    @Override
    public PasswordlessAuthenticationToken saveToken(final PasswordlessUserAccount passwordlessAccount,
                                                     final PasswordlessAuthenticationRequest passwordlessRequest,
                                                     final PasswordlessAuthenticationToken authnToken) {
        return FunctionUtils.doUnchecked(() -> {
            val entity = MongoDbPasswordlessAuthenticationEntity.builder()
                .expirationDate(authnToken.getExpirationDate())
                .username(hashUsername(authnToken.getUsername()))
                .record(encodeToken(authnToken))
                .build();
            val result = mongoTemplate.save(entity, properties.getCollection());
            return authnToken.withId(result.getId());
        });
    }

    @Override
    public void clean() {
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        LOGGER.debug("Cleaning expired records with an expiration date greater than or equal to [{}]", now);
        val query = new Query().addCriteria(Criteria.where("expirationDate").gte(now));
        mongoTemplate.remove(query, MongoDbPasswordlessAuthenticationEntity.class, properties.getCollection());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @With
    @AllArgsConstructor
    @SuperBuilder
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private static final class MongoDbPasswordlessAuthenticationEntity {
        @Id
        private String id;

        @Indexed
        @JsonProperty
        private String username;

        @JsonProperty
        private ZonedDateTime expirationDate;

        @JsonProperty
        private String record;
    }
}
