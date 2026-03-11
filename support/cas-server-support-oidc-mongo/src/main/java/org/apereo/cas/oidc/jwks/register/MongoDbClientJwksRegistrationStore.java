package org.apereo.cas.oidc.jwks.register;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * This is {@link MongoDbClientJwksRegistrationStore}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class MongoDbClientJwksRegistrationStore implements ClientJwksRegistrationStore {
    private final MongoOperations mongoTemplate;

    private final String collectionName;

    @Override
    public ClientJwksRegistrationEntry save(final String jkt, final String jwk) {
        val entry = new ClientJwksRegistrationEntry(jkt, jwk, Instant.now(Clock.systemUTC()));
        val result = mongoTemplate.findById(entry.jkt(), ClientJwksRegistrationEntry.class, collectionName);
        Optional.ofNullable(result)
            .ifPresentOrElse(entity -> {
                val update = Update.update("jwk", jwk);
                val query = new Query(Criteria.where("jkt").is(entry.jkt()));
                mongoTemplate.updateFirst(query, update, collectionName);
            },
                () -> mongoTemplate.insert(entry, collectionName));
        return entry;
    }

    @Override
    public Optional<ClientJwksRegistrationEntry> findByJkt(final String jkt) {
        return Optional.ofNullable(mongoTemplate.findById(jkt, ClientJwksRegistrationEntry.class, collectionName));
    }

    @Override
    public List<ClientJwksRegistrationEntry> load() {
        return mongoTemplate.findAll(ClientJwksRegistrationEntry.class, collectionName);
    }

    @Override
    public void removeByJkt(final String jkt) {
        val query = new Query(Criteria.where("jkt").is(jkt));
        mongoTemplate.remove(query, ClientJwksRegistrationEntry.class, collectionName);
    }

    @Override
    public void removeAll() {
        mongoTemplate.dropCollection(collectionName);
    }
}
