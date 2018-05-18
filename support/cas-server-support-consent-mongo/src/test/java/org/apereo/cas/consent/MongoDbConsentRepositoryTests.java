package org.apereo.cas.consent;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.config.CasConsentMongoDbConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.ConditionalSpringRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * This is {@link MongoDbConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(ConditionalSpringRunner.class)
@SpringBootTest(classes = {
    CasConsentMongoDbConfiguration.class,
    CasConsentCoreConfiguration.class,
    CasCoreAuditConfiguration.class,
    RefreshAutoConfiguration.class
})
@TestPropertySource(locations = "classpath:mongo-consent.properties")
public class MongoDbConsentRepositoryTests {
    @Autowired
    @Qualifier("consentRepository")
    private ConsentRepository consentRepository;

    @Autowired
    @Qualifier("consentDecisionBuilder")
    private ConsentDecisionBuilder consentDecisionBuilder;

    @Test
    public void verifyAction() {
        assertTrue(consentRepository.findConsentDecisions().isEmpty());
        final ConsentDecision decision = consentDecisionBuilder.build(CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService(), "casuser",
            CollectionUtils.wrap("givenName", "CAS"));
        consentRepository.storeConsentDecision(decision);
        assertFalse(consentRepository.findConsentDecisions().isEmpty());
        assertFalse(consentRepository.findConsentDecisions("casuser").isEmpty());
        consentRepository.deleteConsentDecision(decision.getId(), "casuser");
        assertTrue(consentRepository.findConsentDecisions().isEmpty());
    }
}
