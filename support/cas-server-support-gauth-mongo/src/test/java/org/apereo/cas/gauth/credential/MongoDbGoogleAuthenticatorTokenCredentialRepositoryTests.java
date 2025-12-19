package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.config.CasGoogleAuthenticatorMongoDbAutoConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @BeforeEach
    void cleanUp() {
        registry.deleteAll();
    }
}
