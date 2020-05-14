package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AttributeConsentReportEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    AbstractCasEndpointTests.SharedTestConfiguration.class,
    CasConsentCoreConfiguration.class
},
    properties = {
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.attributeConsent.enabled=true"
    })
@Tag("Simple")
public class AttributeConsentReportEndpointTests {
    @Autowired
    @Qualifier("attributeConsentReportEndpoint")
    private AttributeConsentReportEndpoint attributeConsentReportEndpoint;

    @Test
    public void verifyOperation() {
        val results = attributeConsentReportEndpoint.consentDecisions("casuser");
        assertNotNull(results);
    }
}
