package org.apereo.cas.web.flow.actions;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link InjectResponseHeadersActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreUtilConfiguration.class
})
@Tag("WebflowActions")
public class InjectResponseHeadersActionTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("urlValidator")
    private UrlValidator urlValidator;

    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());

        val locator = mock(ResponseBuilderLocator.class);
        when(locator.locate(any(WebApplicationService.class)))
            .thenReturn(new WebApplicationServiceResponseBuilder(this.servicesManager, this.urlValidator));

        val redirectToServiceAction = new InjectResponseHeadersAction(locator);
        val event = redirectToServiceAction.execute(context);
        assertEquals(CasWebflowConstants.STATE_ID_SUCCESS, event.getId());
        assertNotNull(response.getHeader(CasProtocolConstants.PARAMETER_SERVICE));
    }

    @Test
    public void verifyRedirectAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());

        val attributes = new HashMap<String, String>();
        attributes.put(Response.ResponseType.REDIRECT.name().toLowerCase(), "true");

        val casResponse = mock(Response.class);
        when(casResponse.getUrl()).thenReturn("https://google.com");
        when(casResponse.getResponseType()).thenReturn(Response.ResponseType.REDIRECT);
        when(casResponse.getAttributes()).thenReturn(attributes);
        val responseBuilder = mock(ResponseBuilder.class);
        when(responseBuilder.build(any(), any(), any())).thenReturn(casResponse);
        val locator = mock(ResponseBuilderLocator.class);
        when(locator.locate(any(WebApplicationService.class))).thenReturn(responseBuilder);

        val redirectToServiceAction = new InjectResponseHeadersAction(locator);
        val event = redirectToServiceAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, event.getId());
        assertNotNull(response.getHeader(CasProtocolConstants.PARAMETER_SERVICE));
    }
}
