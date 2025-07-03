package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.consent.ConsentableAttributeBuilder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CheckConsentRequiredActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
@Import(CheckConsentRequiredActionTests.ConsentTestConfiguration.class)
class CheckConsentRequiredActionTests extends BaseConsentActionTests {

    @BeforeEach
    void beforeEach() {
        servicesManager.deleteAll();
    }

    @Test
    void verifyNoConsentWithoutServiceOrAuthn() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        assertNull(checkConsentRequiredAction.execute(context));

        val id = UUID.randomUUID().toString();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
        servicesManager.save(registeredService);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService(registeredService.getServiceId()));

        assertNull(checkConsentRequiredAction.execute(context));
    }

    @Test
    void verifyOperationGlobalConsentActive() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);

        val id = UUID.randomUUID().toString();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
        val attrPolicy = new ReturnAllAttributeReleasePolicy();
        attrPolicy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        registeredService.setAttributeReleasePolicy(attrPolicy);
        servicesManager.save(registeredService);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService(registeredService.getServiceId()));
        assertEquals(CheckConsentRequiredAction.EVENT_ID_CONSENT_REQUIRED, checkConsentRequiredAction.execute(context).getId());
        val flowScope = context.getFlowScope();
        assertTrue(flowScope.contains("attributes"));
        assertTrue(flowScope.contains("principal"));
        assertTrue(flowScope.contains("service"));
        assertTrue(flowScope.contains("option"));
        assertTrue(flowScope.contains("reminder"));
        assertTrue(flowScope.contains("reminderTimeUnit"));
    }

    @Test
    void verifyOperationServiceEnabled() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        val registeredService = getRegisteredServiceWithConsentStatus(TriStateBoolean.TRUE);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService(registeredService.getServiceId()));
        assertEquals(CheckConsentRequiredAction.EVENT_ID_CONSENT_REQUIRED, checkConsentRequiredAction.execute(context).getId());
    }

    @Test
    void verifyOperationServiceDisabled() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        val registeredService = getRegisteredServiceWithConsentStatus(TriStateBoolean.FALSE);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService(registeredService.getServiceId()));
        assertNull(checkConsentRequiredAction.execute(context));
    }

    private RegisteredService getRegisteredServiceWithConsentStatus(final TriStateBoolean status) {
        val id = UUID.randomUUID().toString();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
        val attrPolicy = new ReturnAllAttributeReleasePolicy();
        attrPolicy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy().setStatus(status));
        registeredService.setAttributeReleasePolicy(attrPolicy);
        servicesManager.save(registeredService);
        return registeredService;
    }

    @TestConfiguration(value = "ConsentTestConfiguration", proxyBeanMethods = false)
    static class ConsentTestConfiguration {
        @Bean
        public ConsentableAttributeBuilder testConsentableAttributeBuilder() {
            return attribute -> attribute;
        }
    }
}

