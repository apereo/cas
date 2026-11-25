package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.multitenancy.TenantsManager;
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
import org.springframework.context.ConfigurableApplicationContext;
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

    @Autowired
    @Qualifier(ConsentRepository.BEAN_NAME)
    private ConsentRepository consentRepository;

    @Autowired
    @Qualifier(ConsentDecisionBuilder.BEAN_NAME)
    private ConsentDecisionBuilder consentDecisionBuilder;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

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
        val result = consentEngine.isConsentRequiredFor(service, consentService, authentication);
        assertFalse(result.isRequired());
        assertNull(result.getConsentableAttributes());
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
    void verifyDecisionsStayInTheirTenant() throws Throwable {
        val tenantsManager = mock(TenantsManager.class);
        when(tenantsManager.findTenant(anyString())).thenAnswer(invocation ->
            Optional.of(TenantDefinition.builder().id(invocation.getArgument(0)).build()));
        val tenantExtractor = mock(TenantExtractor.class);
        when(tenantExtractor.getTenantsManager()).thenReturn(tenantsManager);
        val engine = new DefaultConsentEngine(consentRepository, consentDecisionBuilder, casProperties,
            List.of(), applicationContext, tenantExtractor);

        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
        val consentService = RegisteredServiceTestUtils.getRegisteredService("consentService");
        val policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        consentService.setAttributeReleasePolicy(policy);
        val serviceId = "https://consent-%s.example.org".formatted(UUID.randomUUID());
        val shireService = RegisteredServiceTestUtils.getService(serviceId);
        shireService.setTenant("shire");
        val defaultService = RegisteredServiceTestUtils.getService(serviceId);
        val mordorService = RegisteredServiceTestUtils.getService(serviceId);
        mordorService.setTenant("mordor");

        val shireDecision = engine.storeConsentDecision(shireService, consentService, authentication,
            14, ChronoUnit.DAYS, ConsentReminderOptions.ATTRIBUTE_NAME);
        assertEquals("shire", shireDecision.getTenant());
        assertFalse(engine.isConsentRequiredFor(shireService, consentService, authentication).isRequired());
        assertNotNull(engine.findConsentDecision(shireService, consentService, authentication));
        assertNull(engine.findConsentDecision(defaultService, consentService, authentication));
        assertNull(engine.findConsentDecision(mordorService, consentService, authentication));
        assertTrue(engine.isConsentRequiredFor(defaultService, consentService, authentication).isRequired());

        val defaultDecision = engine.storeConsentDecision(defaultService, consentService, authentication,
            14, ChronoUnit.DAYS, ConsentReminderOptions.ATTRIBUTE_NAME);
        assertNull(defaultDecision.getTenant());
        assertNotEquals(shireDecision.getId(), defaultDecision.getId());
        assertEquals(2, consentRepository.findConsentDecisions(authentication.getPrincipal().getId()).size());
        assertEquals("shire", engine.findConsentDecision(shireService, consentService, authentication).getTenant());
        assertNull(engine.findConsentDecision(defaultService, consentService, authentication).getTenant());
        assertFalse(engine.isConsentRequiredFor(defaultService, consentService, authentication).isRequired());
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
    void verifyReminderThatCannotBeAppliedRequiresConsent() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
        val service = CoreAuthenticationTestUtils.getService();
        val consentService = RegisteredServiceTestUtils.getRegisteredService("consentService");
        val policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        consentService.setAttributeReleasePolicy(policy);
        assertNotNull(consentEngine.storeConsentDecision(service, consentService,
            authentication, 1, ChronoUnit.FOREVER, ConsentReminderOptions.ATTRIBUTE_NAME));
        val result = assertDoesNotThrow(() -> consentEngine.isConsentRequiredFor(service, consentService, authentication));
        assertTrue(result.isRequired());
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
        assertNotNull(result.getConsentableAttributes());
        assertFalse(result.getConsentableAttributes().isEmpty());
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
