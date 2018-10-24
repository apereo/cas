package org.apereo.cas.web.flow.actions;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link InjectResponseHeadersActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class
})
public class InjectResponseHeadersActionTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());

        val locator = mock(ResponseBuilderLocator.class);
        when(locator.locate(any(WebApplicationService.class))).thenReturn(new WebApplicationServiceResponseBuilder(this.servicesManager));

        val redirectToServiceAction = new InjectResponseHeadersAction(locator);
        val event = redirectToServiceAction.execute(context);
        assertEquals(CasWebflowConstants.STATE_ID_SUCCESS, event.getId());
        assertNotNull(response.getHeader(CasProtocolConstants.PARAMETER_SERVICE));
    }
}
