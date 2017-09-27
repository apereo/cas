package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentApiConfiguration;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultConsentDecisionBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        CasConsentApiConfiguration.class,
        CasConsentCoreConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreUtilConfiguration.class})
public class DefaultConsentDecisionBuilderTests {

    @Autowired
    @Qualifier("consentDecisionBuilder")
    private ConsentDecisionBuilder consentDecisionBuilder;

    @Test
    public void verifyNewConsentDecision() {
        final ConsentDecision consentDecision = getConsentDecision();
        assertNotNull(consentDecision);
        assertEquals(consentDecision.getPrincipal(), "casuser");
        assertEquals(consentDecision.getService(), RegisteredServiceTestUtils.getService().getId());    
    }

    @Test
    public void verifyAttributesRequireConsent() {
        final ConsentDecision consentDecision = getConsentDecision();
        assertTrue(consentDecisionBuilder.doesAttributeReleaseRequireConsent(consentDecision, CollectionUtils.wrap("attr2", "value2")));
        assertFalse(consentDecisionBuilder.doesAttributeReleaseRequireConsent(consentDecision, CollectionUtils.wrap("attr1", "something")));
    }

    @Test
    public void verifyAttributeValuesRequireConsent() {
        final ConsentDecision consentDecision = getConsentDecision();
        consentDecision.setOptions(ConsentOptions.ATTRIBUTE_VALUE);
        assertTrue(consentDecisionBuilder.doesAttributeReleaseRequireConsent(consentDecision, CollectionUtils.wrap("attr1", "value2")));
    }

    @Test
    public void verifyAttributesAreRetrieved() {
        final ConsentDecision consentDecision = getConsentDecision();
        final Map<String, Object> attrs = consentDecisionBuilder.getConsentableAttributesFrom(consentDecision);
        assertTrue(attrs.containsKey("attr1"));
        assertEquals(attrs.get("attr1"), "value1");
    }
    
    private ConsentDecision getConsentDecision() {
        return consentDecisionBuilder.build(RegisteredServiceTestUtils.getService(),
                RegisteredServiceTestUtils.getRegisteredService("test"),
                "casuser", CollectionUtils.wrap("attr1", "value1"));
    }

}
