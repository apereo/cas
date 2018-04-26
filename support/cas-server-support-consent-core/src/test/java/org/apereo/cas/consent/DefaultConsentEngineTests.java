package org.apereo.cas.consent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasConsentApiConfiguration;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultConsentEngineTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    CasRegisteredServicesTestConfiguration.class,
    CasConsentApiConfiguration.class,
    CasConsentCoreConfiguration.class,
    CasCoreAuditConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class})
@Slf4j
@DirtiesContext
public class DefaultConsentEngineTests {

    @Autowired
    @Qualifier("consentEngine")
    private ConsentEngine consentEngine;

    @Test
    public void verifyConsentIsAlwaysRequired() {
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        final Service service = CoreAuthenticationTestUtils.getService();
        final RegisteredService consentService = CoreAuthenticationTestUtils.getRegisteredService("consentService");
        final ReturnAllAttributeReleasePolicy policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        when(consentService.getAttributeReleasePolicy()).thenReturn(policy);
        final ConsentDecision decision = this.consentEngine.storeConsentDecision(service, consentService,
            authentication, 14, ChronoUnit.DAYS, ConsentReminderOptions.ALWAYS);
        assertNotNull(decision);
        final Pair<Boolean, ConsentDecision> result = this.consentEngine.isConsentRequiredFor(service, consentService, authentication);
        assertNotNull(result);
        assertTrue(result.getKey());
        assertEquals(decision, result.getRight());
    }

    @Test
    public void verifyConsentIsRequiredByAttributeName() {
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        final Service service = CoreAuthenticationTestUtils.getService();
        final RegisteredService consentService = CoreAuthenticationTestUtils.getRegisteredService("consentService");
        final ReturnAllAttributeReleasePolicy policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        when(consentService.getAttributeReleasePolicy()).thenReturn(policy);
        final ConsentDecision decision = this.consentEngine.storeConsentDecision(service, consentService,
            authentication, 14, ChronoUnit.DAYS, ConsentReminderOptions.ATTRIBUTE_NAME);
        assertNotNull(decision);
        final Pair<Boolean, ConsentDecision> result = this.consentEngine.isConsentRequiredFor(service, consentService, authentication);
        assertNotNull(result);
        assertFalse(result.getKey());
    }

    @Test
    public void verifyConsentFound() {
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        final Service service = CoreAuthenticationTestUtils.getService();
        final RegisteredService consentService = CoreAuthenticationTestUtils.getRegisteredService("consentService");
        final ReturnAllAttributeReleasePolicy policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        when(consentService.getAttributeReleasePolicy()).thenReturn(policy);
        final ConsentDecision decision = this.consentEngine.storeConsentDecision(service, consentService,
            authentication, 14, ChronoUnit.DAYS, ConsentReminderOptions.ATTRIBUTE_NAME);
        assertNotNull(decision);
        final ConsentDecision decision2 = this.consentEngine.findConsentDecision(service, consentService, authentication);
        assertEquals(decision, decision2);
    }
}
