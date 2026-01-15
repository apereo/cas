package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultConsentEngineTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseConsentRepositoryTests.SharedTestConfiguration.class)
@Tag("Consent")
@ExtendWith(CasTestExtension.class)
class DefaultConsentEngineTests {
    @Autowired
    @Qualifier(ConsentEngine.BEAN_NAME)
    private ConsentEngine consentEngine;

    @BeforeAll
    public static void beforeAll() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.setLocalAddr("127.0.0.1");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyConsentDisablesRelease() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
        val service = CoreAuthenticationTestUtils.getService();
        val consentService = RegisteredServiceTestUtils.getRegisteredService("consentService");
        consentService.setAttributeReleasePolicy(null);
        assertTrue(consentEngine.resolveConsentableAttributesFrom(authentication, service, consentService).isEmpty());
        assertFalse(consentEngine.isConsentRequiredFor(service, consentService, authentication).isRequired());
    }

    @Test
    void verifyConsentIgnored() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
        val service = CoreAuthenticationTestUtils.getService();
        val consentService = RegisteredServiceTestUtils.getRegisteredService("consentService");
        val policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        consentService.setAttributeReleasePolicy(policy);
        assertTrue(consentEngine.isConsentRequiredFor(service, consentService, authentication).isRequired());
    }

    @Test
    void verifyConsentExpired() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
        val service = CoreAuthenticationTestUtils.getService();
        val consentService = RegisteredServiceTestUtils.getRegisteredService("consentService");
        val policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        consentService.setAttributeReleasePolicy(policy);
        consentEngine.storeConsentDecision(service, consentService,
            authentication, -20, ChronoUnit.MONTHS, ConsentReminderOptions.ATTRIBUTE_NAME);
        assertTrue(consentEngine.isConsentRequiredFor(service, consentService, authentication).isRequired());
    }

    @Test
    void verifyConsentIsAlwaysRequired() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
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
        assertTrue(result.isRequired());
        assertEquals(decision, result.getConsentDecision());
    }

    @Test
    void verifyConsentIsRequiredByAttributeName() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
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
        assertFalse(result.isRequired());
    }

    @Test
    void verifyConsentFound() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
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
