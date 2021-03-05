package org.apereo.cas.consent;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AttributeConsentReportEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "management.endpoint.attributeConsent.enabled=true")
@Tag("ActuatorEndpoint")
@Import(CasConsentCoreConfiguration.class)
public class AttributeConsentReportEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("attributeConsentReportEndpoint")
    private AttributeConsentReportEndpoint attributeConsentReportEndpoint;

    @Autowired
    @Qualifier("consentRepository")
    private ConsentRepository consentRepository;

    @Autowired
    @Qualifier("consentDecisionBuilder")
    private ConsentDecisionBuilder consentDecisionBuilder;

    @Test
    public void verifyOperation() {
        val desc = consentDecisionBuilder.build(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getRegisteredService(), "casuser",
            CoreAuthenticationTestUtils.getAttributes());
        consentRepository.storeConsentDecision(desc);

        var results = attributeConsentReportEndpoint.consentDecisions("casuser");
        assertFalse(results.isEmpty());

        assertTrue(attributeConsentReportEndpoint.revokeConsents(desc.getPrincipal(), desc.getId()));
        results = attributeConsentReportEndpoint.consentDecisions("casuser");
        assertTrue(results.isEmpty());

    }
}
