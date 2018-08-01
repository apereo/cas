package org.apereo.cas.consent;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.category.MongoDbCategory;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.config.CasConsentMongoDbConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link MongoDbConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasConsentMongoDbConfiguration.class,
    CasConsentCoreConfiguration.class,
    CasCoreAuditConfiguration.class,
    RefreshAutoConfiguration.class
})
@Category(MongoDbCategory.class)
@TestPropertySource(properties = {
    "cas.consent.mongo.host=localhost",
    "cas.consent.mongo.port=8081",
    "cas.consent.mongo.dropCollection=true",
    "cas.consent.mongo.databaseName=consent"
    })
public class MongoDbConsentRepositoryTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    @Qualifier("consentRepository")
    private ConsentRepository consentRepository;

    @Autowired
    @Qualifier("consentDecisionBuilder")
    private ConsentDecisionBuilder consentDecisionBuilder;

    @Test
    public void verifyAction() {
        assertTrue(consentRepository.findConsentDecisions().isEmpty());
        val decision = consentDecisionBuilder.build(CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService(), "casuser",
            CollectionUtils.wrap("givenName", "CAS"));
        consentRepository.storeConsentDecision(decision);
        assertFalse(consentRepository.findConsentDecisions().isEmpty());
        assertFalse(consentRepository.findConsentDecisions("casuser").isEmpty());
        consentRepository.deleteConsentDecision(decision.getId(), "casuser");
        assertTrue(consentRepository.findConsentDecisions().isEmpty());
    }
}
