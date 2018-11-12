package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.test.MockRequestContext;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Mockito based tests for @{link ServiceAuthorizationCheck}
 *
 * @author Dmitriy Kopylenko
 * @since 3.5.0
 */
public class ServiceAuthorizationCheckTests {
    private final WebApplicationService authorizedService = mock(WebApplicationService.class);
    private final WebApplicationService unauthorizedService = mock(WebApplicationService.class);
    private final WebApplicationService undefinedService = mock(WebApplicationService.class);
    private final ServicesManager servicesManager = mock(ServicesManager.class);
    private ServiceAuthorizationCheckAction serviceAuthorizationCheck;

    @BeforeEach
    public void setUpMocks() {
        val authorizedRegisteredService = new RegexRegisteredService();
        val unauthorizedRegisteredService = new RegexRegisteredService();
        unauthorizedRegisteredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));

        val list = new ArrayList<RegisteredService>();
        list.add(authorizedRegisteredService);
        list.add(unauthorizedRegisteredService);

        when(this.servicesManager.findServiceBy(this.authorizedService)).thenReturn(authorizedRegisteredService);
        when(this.servicesManager.findServiceBy(this.unauthorizedService)).thenReturn(unauthorizedRegisteredService);
        when(this.servicesManager.findServiceBy(this.undefinedService)).thenReturn(null);
        when(this.servicesManager.getAllServices()).thenReturn((Collection) list);

        this.serviceAuthorizationCheck = new ServiceAuthorizationCheckAction(this.servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
    }

    @Test
    public void noServiceProvided() {
        val mockRequestContext = new MockRequestContext();
        val event = this.serviceAuthorizationCheck.doExecute(mockRequestContext);
        assertEquals("success", event.getId());
    }

    @Test
    public void authorizedServiceProvided() {
        val mockRequestContext = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(mockRequestContext, authorizedService);
        val event = this.serviceAuthorizationCheck.doExecute(mockRequestContext);
        assertEquals("success", event.getId());
    }

    @Test
    public void unauthorizedServiceProvided() {
        val mockRequestContext = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(mockRequestContext, unauthorizedService);

        assertThrows(UnauthorizedServiceException.class, () -> this.serviceAuthorizationCheck.doExecute(mockRequestContext));
    }

    @Test
    public void serviceThatIsNotRegisteredProvided() {
        val mockRequestContext = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(mockRequestContext, undefinedService);

        assertThrows(UnauthorizedServiceException.class, () -> this.serviceAuthorizationCheck.doExecute(mockRequestContext));
    }
}
