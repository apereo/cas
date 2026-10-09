package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.config.CasGoogleAuthenticatorMongoDbAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    CasGoogleAuthenticatorMongoDbAutoConfiguration.class,
    BaseOneTimeTokenCredentialRepositoryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.mfa.gauth.mongo.host=localhost",
        "cas.authn.mfa.gauth.mongo.port=27017",
        "cas.authn.mfa.gauth.mongo.drop-collection=true",
        "cas.authn.mfa.gauth.mongo.user-id=root",
        "cas.authn.mfa.gauth.mongo.password=secret",
        "cas.authn.mfa.gauth.mongo.authentication-database-name=admin",
        "cas.authn.mfa.gauth.mongo.database-name=gauth-token-credential",
        "cas.authn.mfa.gauth.core.scratch-codes.encryption.key=12345678901234567890123456789012",
        "cas.authn.mfa.gauth.crypto.enabled=false"
    })
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
@Tag("MongoDbMFA")
@ExtendWith(CasTestExtension.class)
@Getter
@EnabledIfListeningOnPort(port = 27017)
class MongoDbGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {
    @Autowired
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository registry;

    @Autowired
    @Qualifier("mongoDbGoogleAuthenticatorTemplate")
    private MongoOperations mongoTemplate;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeEach
    void cleanUp() {
        registry.deleteAll();
    }

    @Test
    void verifyUsernamesDifferingByAccentAreDistinct() {
        val suffix = UUID.randomUUID().toString();
        registry.save(registry.create("jose" + suffix));
        assertEquals(1, registry.count("JOSE" + suffix));
        assertEquals(0, registry.count("jos\u00e9" + suffix));
    }

    @Test
    void verifyUsernameIndexCarriesCollation() {
        val collection = casProperties.getAuthn().getMfa().getGauth().getMongo().getCollection();
        val index = mongoTemplate.indexOps(collection).getIndexInfo()
            .stream()
            .filter(info -> MongoDbGoogleAuthenticatorTokenCredentialRepository.USERNAME_INDEX_NAME.equals(info.getName()))
            .findFirst()
            .orElseThrow();
        assertEquals(2, ((Number) index.getCollation().orElseThrow().get("strength")).intValue());
        assertEquals("en", index.getCollation().orElseThrow().get("locale"));
    }
}
