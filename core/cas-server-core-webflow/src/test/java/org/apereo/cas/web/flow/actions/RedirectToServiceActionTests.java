package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RedirectToServiceActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowServiceActions")
class RedirectToServiceActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(UrlValidator.BEAN_NAME)
    private UrlValidator urlValidator;

    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());

        val locator = mock(ResponseBuilderLocator.class);
        when(locator.locate(any(WebApplicationService.class)))
            .thenReturn(new WebApplicationServiceResponseBuilder(this.servicesManager, this.urlValidator));

        val redirectToServiceAction = new RedirectToServiceAction(locator);
        val event = redirectToServiceAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, event.getId());
    }
}
