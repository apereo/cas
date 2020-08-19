package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CheckConsentRequiredActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
public class CheckConsentRequiredActionTests extends BaseConsentActionTests {

    @BeforeEach
    public void beforeEach() {
        servicesManager.deleteAll();
    }

    @Test
    public void verifyOperationGlobalConsentActive() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

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
    public void verifyOperationServiceEnabled() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        val registeredService = getRegisteredServiceWithConsentStatus(TriStateBoolean.TRUE);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService(registeredService.getServiceId()));
        assertEquals(CheckConsentRequiredAction.EVENT_ID_CONSENT_REQUIRED, checkConsentRequiredAction.execute(context).getId());
    }

    @Test
    public void verifyOperationServiceDisabled() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
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
}

