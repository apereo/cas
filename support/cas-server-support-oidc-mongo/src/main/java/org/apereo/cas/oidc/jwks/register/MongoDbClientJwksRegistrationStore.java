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
    public ClientJwksRegistrationEntry save(final String clientId, final String jkt, final String jwk) {
        val entry = new ClientJwksRegistrationEntry(jkt, clientId, jwk, Instant.now(Clock.systemUTC()));
        val result = mongoTemplate.findById(entry.jkt(), ClientJwksRegistrationEntry.class, collectionName);
        Optional.ofNullable(result)
            .ifPresentOrElse(entity -> {
                val update = Update.update("jwk", jwk);
                val query = new Query(Criteria.where("jkt").is(entry.jkt()).and("clientId").is(entry.clientId()));
                mongoTemplate.updateFirst(query, update, ClientJwksRegistrationEntry.class, collectionName);
            },
                () -> mongoTemplate.insert(entry, collectionName));
        return entry;
    }

    @Override
    public Optional<ClientJwksRegistrationEntry> findBy(final String clientId, final String jkt) {
        val query = new Query(Criteria.where("jkt").is(jkt).and("clientId").is(clientId));
        return Optional.ofNullable(mongoTemplate.findOne(query, ClientJwksRegistrationEntry.class, collectionName));
    }

    @Override
    public List<ClientJwksRegistrationEntry> load() {
        return mongoTemplate.findAll(ClientJwksRegistrationEntry.class, collectionName);
    }

    @Override
    public void remove(final String clientId, final String jkt) {
        val query = new Query(Criteria.where("jkt").is(jkt).and("clientId").is(clientId));
        mongoTemplate.remove(query, ClientJwksRegistrationEntry.class, collectionName);
    }

    @Override
    public void removeAll() {
        mongoTemplate.dropCollection(collectionName);
    }
}
