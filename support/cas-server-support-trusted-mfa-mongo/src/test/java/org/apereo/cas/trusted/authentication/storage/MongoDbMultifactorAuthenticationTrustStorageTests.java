package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.category.MongoDbCategory;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.config.MongoDbMultifactorAuthenticationTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * This is {@link MongoDbMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Category(MongoDbCategory.class)
@SpringBootTest(classes = {
    MongoDbMultifactorAuthenticationTrustConfiguration.class,
    MultifactorAuthnTrustedDeviceFingerprintConfiguration.class,
    MultifactorAuthnTrustConfiguration.class,
    CasCoreAuditConfiguration.class,
    RefreshAutoConfiguration.class})
@TestPropertySource(properties = {
    "cas.authn.mfa.trusted.mongo.databaseName=mfa-trusted",
    "cas.authn.mfa.trusted.mongo.host=localhost",
    "cas.authn.mfa.trusted.mongo.port=27017",
    "cas.authn.mfa.trusted.mongo.userId=root",
    "cas.authn.mfa.trusted.mongo.password=secret",
    "cas.authn.mfa.trusted.mongo.authenticationDatabaseName=admin",
    "cas.authn.mfa.trusted.mongo.dropCollection=true"
    })
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 27017)
public class MongoDbMultifactorAuthenticationTrustStorageTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    @Qualifier("mfaTrustEngine")
    private MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Test
    public void verifySetAnExpireByKey() {
        mfaTrustEngine.set(MultifactorAuthenticationTrustRecord.newInstance("casuser", "geography", "fingerprint"));
        val records = mfaTrustEngine.get("casuser");
        assertEquals(1, records.size());
        mfaTrustEngine.expire(records.stream().findFirst().get().getRecordKey());
        assertTrue(mfaTrustEngine.get("casuser").isEmpty());
    }

    @Test
    public void verifyExpireByDate() {
        val r = MultifactorAuthenticationTrustRecord.newInstance("castest", "geography", "fingerprint");
        r.setRecordDate(LocalDateTime.now().minusDays(2));
        mfaTrustEngine.set(r);

        assertThat(mfaTrustEngine.get(LocalDateTime.now().minusDays(30)), hasSize(1));
        assertThat(mfaTrustEngine.get(LocalDateTime.now().minusDays(2)), hasSize(0));
    }
}
