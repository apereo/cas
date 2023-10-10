package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.execution.Action;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Mockito based tests for {@link ServiceAuthorizationCheckAction}
 *
 * @author Dmitriy Kopylenko
 * @since 3.5.0
 */
@Tag("WebflowServiceActions")
class ServiceAuthorizationCheckMockitoActionTests {
    private final WebApplicationService authorizedService = mock(WebApplicationService.class);

    private final WebApplicationService unauthorizedService = mock(WebApplicationService.class);

    private final WebApplicationService undefinedService = mock(WebApplicationService.class);

    private final ServicesManager servicesManager = mock(ServicesManager.class);

    private Action getAction() {
        val authorizedRegisteredService = new CasRegisteredService();
        val unauthorizedRegisteredService = new CasRegisteredService();
        unauthorizedRegisteredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));

        val list = new ArrayList<RegisteredService>();
        list.add(authorizedRegisteredService);
        list.add(unauthorizedRegisteredService);

        when(this.servicesManager.findServiceBy(this.authorizedService)).thenReturn(authorizedRegisteredService);
        when(this.servicesManager.findServiceBy(this.unauthorizedService)).thenReturn(unauthorizedRegisteredService);
        when(this.servicesManager.findServiceBy(this.undefinedService)).thenReturn(null);
        when(this.servicesManager.getAllServices()).thenReturn(list);

        return new ServiceAuthorizationCheckAction(this.servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
    }

    @Test
    void noServiceProvided() throws Exception {
        val mockRequestContext = MockRequestContext.create();
        val action = getAction();
        val event = action.execute(mockRequestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

    @Test
    void verifyEmptyRegistry() throws Throwable {
        val mockRequestContext = MockRequestContext.create();
        WebUtils.putServiceIntoFlowScope(mockRequestContext, RegisteredServiceTestUtils.getService());
        when(servicesManager.getAllServices()).thenReturn(List.of());
        val action = new ServiceAuthorizationCheckAction(servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
        assertThrows(UnauthorizedServiceException.class, () -> action.execute(mockRequestContext));
    }

    @Test
    void authorizedServiceProvided() throws Exception {
        val mockRequestContext = MockRequestContext.create();
        WebUtils.putServiceIntoFlowScope(mockRequestContext, authorizedService);
        val action = getAction();
        val event = action.execute(mockRequestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

    @Test
    void unauthorizedServiceProvided() throws Exception {
        val mockRequestContext = MockRequestContext.create();
        WebUtils.putServiceIntoFlowScope(mockRequestContext, unauthorizedService);

        val action = getAction();
        assertThrows(UnauthorizedServiceException.class, () -> action.execute(mockRequestContext));
    }

    @Test
    void serviceThatIsNotRegisteredProvided() throws Exception {
        val mockRequestContext = MockRequestContext.create();
        WebUtils.putServiceIntoFlowScope(mockRequestContext, undefinedService);
        val action = getAction();
        assertThrows(UnauthorizedServiceException.class, () -> action.execute(mockRequestContext));
    }
}
