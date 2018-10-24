package org.apereo.cas.consent;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;

import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultConsentEngineTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasRegisteredServicesTestConfiguration.class,
    CasConsentCoreConfiguration.class,
    CasCoreAuditConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class})
@DirtiesContext
public class DefaultConsentEngineTests {
    @Autowired
    @Qualifier("consentEngine")
    private ConsentEngine consentEngine;

    @Test
    public void verifyConsentIsAlwaysRequired() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        val service = CoreAuthenticationTestUtils.getService();
        val consentService = CoreAuthenticationTestUtils.getRegisteredService("consentService");
        val policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        when(consentService.getAttributeReleasePolicy()).thenReturn(policy);
        val decision = this.consentEngine.storeConsentDecision(service, consentService,
            authentication, 14, ChronoUnit.DAYS, ConsentReminderOptions.ALWAYS);
        assertNotNull(decision);
        val result = this.consentEngine.isConsentRequiredFor(service, consentService, authentication);
        assertNotNull(result);
        assertTrue(result.getKey());
        assertEquals(decision, result.getRight());
    }

    @Test
    public void verifyConsentIsRequiredByAttributeName() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        val service = CoreAuthenticationTestUtils.getService();
        val consentService = CoreAuthenticationTestUtils.getRegisteredService("consentService");
        val policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        when(consentService.getAttributeReleasePolicy()).thenReturn(policy);
        val decision = this.consentEngine.storeConsentDecision(service, consentService,
            authentication, 14, ChronoUnit.DAYS, ConsentReminderOptions.ATTRIBUTE_NAME);
        assertNotNull(decision);
        val result = this.consentEngine.isConsentRequiredFor(service, consentService, authentication);
        assertNotNull(result);
        assertFalse(result.getKey());
    }

    @Test
    public void verifyConsentFound() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        val service = CoreAuthenticationTestUtils.getService();
        val consentService = CoreAuthenticationTestUtils.getRegisteredService("consentService");
        val policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        when(consentService.getAttributeReleasePolicy()).thenReturn(policy);
        val decision = this.consentEngine.storeConsentDecision(service, consentService,
            authentication, 14, ChronoUnit.DAYS, ConsentReminderOptions.ATTRIBUTE_NAME);
        assertNotNull(decision);
        val decision2 = this.consentEngine.findConsentDecision(service, consentService, authentication);
        assertEquals(decision, decision2);
    }
}
