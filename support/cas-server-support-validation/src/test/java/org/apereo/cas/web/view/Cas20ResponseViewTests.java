package org.apereo.cas.web.view;

import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.support.NoOpProtocolAttributeEncoder;
import org.apereo.cas.config.CasThymeleafConfiguration;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizer;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizerConfigurer;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.AbstractServiceValidateController;
import org.apereo.cas.web.AbstractServiceValidateControllerTests;
import org.apereo.cas.web.MockRequestedAuthenticationContextValidator;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
import org.apereo.cas.web.ServiceValidationViewFactory;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.v2.ServiceValidateController;
import org.apereo.cas.web.view.attributes.NoOpProtocolAttributesRenderer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Cas20ResponseView}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@SpringBootTest(classes = {
    Cas20ResponseViewTests.Cas20ResponseViewTestConfiguration.class,
    BaseCasCoreTests.SharedTestConfiguration.class,
    CasThemesConfiguration.class,
    CasThymeleafConfiguration.class,
    CasValidationConfiguration.class
})
@Tag("CAS")
public class Cas20ResponseViewTests extends AbstractServiceValidateControllerTests {
    @Autowired
    @Qualifier("serviceValidationViewFactory")
    private ServiceValidationViewFactory serviceValidationViewFactory;

    @Override
    public AbstractServiceValidateController getServiceValidateControllerInstance() {
        val context = ServiceValidateConfigurationContext.builder()
            .validationSpecifications(CollectionUtils.wrapSet(getValidationSpecification()))
            .authenticationSystemSupport(getAuthenticationSystemSupport())
            .servicesManager(getServicesManager())
            .centralAuthenticationService(getCentralAuthenticationService())
            .argumentExtractor(getArgumentExtractor())
            .proxyHandler(getProxyHandler())
            .requestedContextValidator(new MockRequestedAuthenticationContextValidator())
            .authnContextAttribute("authenticationContext")
            .validationAuthorizers(getServiceValidationAuthorizers())
            .renewEnabled(true)
            .validationViewFactory(serviceValidationViewFactory)
            .build();
        return new ServiceValidateController(context);
    }

    @Test
    public void verifyValidationFailsInvalidTicket() throws Exception {
        val service = CoreAuthenticationTestUtils.getWebApplicationService(UUID.randomUUID().toString());
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(service.getId());
        getServicesManager().save(registeredService);
        when(registeredService.getProxyPolicy()).thenReturn(new RefuseRegisteredServiceProxyPolicy());
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, UUID.randomUUID().toString());
        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertNotNull(modelAndView);
        assertNotNull(modelAndView.getView());
        assertTrue(modelAndView.getView().toString().contains("Failure"));
    }

    @Test
    public void verifyValidationTicketAuthzFails() throws Exception {
        val service = CoreAuthenticationTestUtils.getWebApplicationService("not-authorized");
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
        getServicesManager().save(registeredService);
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), service, ctx);
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        
        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertNotNull(modelAndView);
        assertNotNull(modelAndView.getView());
        assertTrue(modelAndView.getView().toString().contains("Failure"));
    }

    @Test
    public void verifyValidationFailsBadProxy() throws Exception {
        val service = CoreAuthenticationTestUtils.getWebApplicationService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
        registeredService.setMatchingStrategy(new PartialRegexRegisteredServiceMatchingStrategy());
        registeredService.setProxyPolicy(new RefuseRegisteredServiceProxyPolicy());
        getServicesManager().save(registeredService);
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), service, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertNotNull(modelAndView);
        assertNotNull(modelAndView.getView());
        assertTrue(modelAndView.getView().toString().contains("Failure"));
    }

    @Test
    public void verifyValidationFailsBadAccess() throws Exception {
        val service = CoreAuthenticationTestUtils.getWebApplicationService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, true));
        registeredService.setMatchingStrategy(new PartialRegexRegisteredServiceMatchingStrategy());
        registeredService.setProxyPolicy(new RefuseRegisteredServiceProxyPolicy());
        getServicesManager().save(registeredService);
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), service, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertNotNull(modelAndView);
        assertNotNull(modelAndView.getView());
        assertTrue(modelAndView.getView().toString().contains("Failure"));
    }

    @Test
    public void verifyView() throws Exception {
        val modelAndView = this.getModelAndViewUponServiceValidationWithSecurePgtUrl(
            RegisteredServiceTestUtils.getService("https://www.casinthecloud.com"));
        val req = new MockHttpServletRequest(new MockServletContext());
        req.setAttribute(RequestContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE,
            new GenericWebApplicationContext(req.getServletContext()));

        val resp = new MockHttpServletResponse();
        val delegatedView = new View() {
            @Override
            public String getContentType() {
                return MediaType.TEXT_HTML_VALUE;
            }

            @Override
            public void render(final Map<String, ?> map, final HttpServletRequest request, final HttpServletResponse response) {
                map.forEach(request::setAttribute);
            }
        };
        val view = new Cas20ResponseView(true, new NoOpProtocolAttributeEncoder(),
            null, delegatedView, new DefaultAuthenticationAttributeReleasePolicy("attribute"),
            new DefaultAuthenticationServiceSelectionPlan(), NoOpProtocolAttributesRenderer.INSTANCE);
        view.render(modelAndView.getModel(), req, resp);

        assertNotNull(req.getAttribute(CasViewConstants.MODEL_ATTRIBUTE_NAME_CHAINED_AUTHENTICATIONS));
        assertNotNull(req.getAttribute(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRIMARY_AUTHENTICATION));
        assertNotNull(req.getAttribute(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL));
        assertNotNull(req.getAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_PROXY_GRANTING_TICKET_IOU));
    }

    @TestConfiguration("Cas20ResponseViewTestConfiguration")
    public static class Cas20ResponseViewTestConfiguration implements ServiceTicketValidationAuthorizerConfigurer {
        @Override
        public void configureAuthorizersExecutionPlan(final ServiceTicketValidationAuthorizersExecutionPlan plan) {
            val authz = mock(ServiceTicketValidationAuthorizer.class);
            doThrow(new IllegalArgumentException()).when(authz).authorize(any(),
                argThat(service -> service.getId().equals("not-authorized")), any());
            plan.registerAuthorizer(authz);
        }
    }
}
