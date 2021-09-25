package org.apereo.cas.gauth.credential;

import org.apereo.cas.config.GoogleAuthenticatorMongoDbConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link MongoDbGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    GoogleAuthenticatorMongoDbConfiguration.class,
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
        "cas.authn.mfa.gauth.crypto.enabled=false"
    })
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableScheduling
@Tag("MongoDb")
@Getter
@EnabledIfPortOpen(port = 27017)
public class MongoDbGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {
    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;

    @BeforeEach
    public void cleanUp() {
        registry.deleteAll();
    }
}
