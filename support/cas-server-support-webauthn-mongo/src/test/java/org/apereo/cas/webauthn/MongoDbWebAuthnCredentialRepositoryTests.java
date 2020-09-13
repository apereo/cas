package org.apereo.cas.webauthn;

import org.apereo.cas.config.MongoDbWebAuthnConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepositoryTests;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MongoDbWebAuthnCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(
    properties = {
        "cas.authn.mfa.web-authn.mongo.host=localhost",
        "cas.authn.mfa.web-authn.mongo.port=27017",
        "cas.authn.mfa.web-authn.mongo.drop-collection=true",
        "cas.authn.mfa.web-authn.mongo.asynchronous=false",
        "cas.authn.mfa.web-authn.mongo.user-id=root",
        "cas.authn.mfa.web-authn.mongo.password=secret",
        "cas.authn.mfa.web-authn.mongo.database-name=mfa",
        "cas.authn.mfa.web-authn.mongo.authentication-database-name=admin"
    })
@Tag("MongoDb")
@Getter
@EnabledIfPortOpen(port = 27017)
@Import(MongoDbWebAuthnConfiguration.class)
public class MongoDbWebAuthnCredentialRepositoryTests extends BaseWebAuthnCredentialRepositoryTests {
    @Autowired
    @Qualifier("mongoWebAuthnTemplate")
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void cleanUp() {
        val query = new Query();
        query.addCriteria(Criteria.where(MongoDbWebAuthnCredentialRegistration.FIELD_USERNAME).exists(true));
        val collection = casProperties.getAuthn().getMfa().getWebAuthn().getMongo().getCollection();
        this.mongoTemplate.remove(query, MongoDbWebAuthnCredentialRegistration.class, collection);
    }

}
