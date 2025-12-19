package org.apereo.cas.webauthn;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yubico.data.CredentialRegistration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * This is {@link MongoDbWebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class MongoDbWebAuthnCredentialRepository extends BaseWebAuthnCredentialRepository {
    private final MongoOperations mongoTemplate;

    public MongoDbWebAuthnCredentialRepository(final MongoOperations mongoTemplate,
                                               final CasConfigurationProperties properties,
                                               final CipherExecutor<String, String> cipherExecutor) {
        super(properties, cipherExecutor);
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUsername(final String username) {
        val query = new Query().addCriteria(Criteria.where(MongoDbWebAuthnCredentialRegistration.FIELD_USERNAME).is(username))
            .collation(Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.primary()));
        val records = mongoTemplate.find(query, MongoDbWebAuthnCredentialRegistration.class,
            getProperties().getAuthn().getMfa().getWebAuthn().getMongo().getCollection());
        return records.stream()
            .map(record -> getCipherExecutor().decode(record.getRecords()))
            .map(Unchecked.function(record -> WebAuthnUtils.getObjectMapper()
                .readValue(record, new TypeReference<Set<CredentialRegistration>>() {
                })))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    @Override
    public Stream<CredentialRegistration> stream() {
        val query = new Query().addCriteria(Criteria.where(MongoDbWebAuthnCredentialRegistration.FIELD_USERNAME).exists(true))
            .collation(Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.primary()));
        val records = mongoTemplate.find(query, MongoDbWebAuthnCredentialRegistration.class,
            getProperties().getAuthn().getMfa().getWebAuthn().getMongo().getCollection());
        return records.stream()
            .map(record -> getCipherExecutor().decode(record.getRecords()))
            .map(Unchecked.function(record -> WebAuthnUtils.getObjectMapper()
                .readValue(record, new TypeReference<Set<CredentialRegistration>>() {
                })))
            .flatMap(Collection::stream);
    }

    @Override
    protected void update(final String username, final Collection<CredentialRegistration> givenRecords) {
        val records = givenRecords
            .stream()
            .map(record -> {
                if (record.getRegistrationTime() == null) {
                    return record.withRegistrationTime(Instant.now(Clock.systemUTC()));
                }
                return record;
            })
            .collect(Collectors.toList());

        val query = new Query(Criteria.where(MongoDbWebAuthnCredentialRegistration.FIELD_USERNAME).is(username))
            .collation(Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.primary()));
        val collection = getProperties().getAuthn().getMfa().getWebAuthn().getMongo().getCollection();
        if (records.isEmpty()) {
            LOGGER.debug("No records are provided for [{}] so entry will be removed", username);
            mongoTemplate.remove(query, MongoDbWebAuthnCredentialRegistration.class, collection);
        } else {
            val jsonRecords = FunctionUtils.doUnchecked(() -> getCipherExecutor().encode(WebAuthnUtils.getObjectMapper().writeValueAsString(records)));
            val entry = MongoDbWebAuthnCredentialRegistration.builder()
                .records(jsonRecords)
                .username(username)
                .build();

            val update = Update.update(MongoDbWebAuthnCredentialRegistration.FIELD_RECORDS, jsonRecords);
            val result = mongoTemplate.updateFirst(query, update, collection);
            if (result.getMatchedCount() <= 0) {
                LOGGER.debug("Storing new registration record for [{}]", username);
                mongoTemplate.save(entry, collection);
            }
        }
    }
}
