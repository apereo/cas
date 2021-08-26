package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

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
@Tag("WebflowActions")
public class ServiceAuthorizationCheckMockitoActionTests {
    private final WebApplicationService authorizedService = mock(WebApplicationService.class);

    private final WebApplicationService unauthorizedService = mock(WebApplicationService.class);

    private final WebApplicationService undefinedService = mock(WebApplicationService.class);

    private final ServicesManager servicesManager = mock(ServicesManager.class);

    private Action getAction() {
        val authorizedRegisteredService = new RegexRegisteredService();
        val unauthorizedRegisteredService = new RegexRegisteredService();
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
    public void noServiceProvided() throws Exception {
        val mockRequestContext = new MockRequestContext();
        val action = getAction();
        val event = action.execute(mockRequestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

    @Test
    public void verifyEmptyRegistry() {
        val mockRequestContext = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(mockRequestContext, RegisteredServiceTestUtils.getService());
        when(servicesManager.getAllServices()).thenReturn(List.of());
        val action = new ServiceAuthorizationCheckAction(servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
        assertThrows(UnauthorizedServiceException.class, () -> action.execute(mockRequestContext));
    }

    @Test
    public void authorizedServiceProvided() throws Exception {
        val mockRequestContext = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(mockRequestContext, authorizedService);
        val action = getAction();
        val event = action.execute(mockRequestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

    @Test
    public void unauthorizedServiceProvided() {
        val mockRequestContext = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(mockRequestContext, unauthorizedService);

        val action = getAction();
        assertThrows(UnauthorizedServiceException.class, () -> action.execute(mockRequestContext));
    }

    @Test
    public void serviceThatIsNotRegisteredProvided() {
        val mockRequestContext = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(mockRequestContext, undefinedService);
        val action = getAction();
        assertThrows(UnauthorizedServiceException.class, () -> action.execute(mockRequestContext));
    }
}
