package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Mockito based tests for @{link ServiceAuthorizationCheck}
 *
 * @author Dmitriy Kopylenko
 * @since 3.5.0
 */
public class ServiceAuthorizationCheckTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ServiceAuthorizationCheck serviceAuthorizationCheck;

    private final WebApplicationService authorizedService = mock(WebApplicationService.class);

    private final WebApplicationService unauthorizedService = mock(WebApplicationService.class);

    private final WebApplicationService undefinedService = mock(WebApplicationService.class);

    private final ServicesManager servicesManager = mock(ServicesManager.class);

    @Before
    public void setUpMocks() {
        final RegexRegisteredService authorizedRegisteredService = new RegexRegisteredService();
        final RegexRegisteredService unauthorizedRegisteredService = new RegexRegisteredService();
        unauthorizedRegisteredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));

        final List<RegisteredService> list = new ArrayList<>();
        list.add(authorizedRegisteredService);
        list.add(unauthorizedRegisteredService);
        
        when(this.servicesManager.findServiceBy(this.authorizedService)).thenReturn(authorizedRegisteredService);
        when(this.servicesManager.findServiceBy(this.unauthorizedService)).thenReturn(unauthorizedRegisteredService);
        when(this.servicesManager.findServiceBy(this.undefinedService)).thenReturn(null);
        when(this.servicesManager.getAllServices()).thenReturn(list);
        
        this.serviceAuthorizationCheck = new ServiceAuthorizationCheck(this.servicesManager, 
                new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
    }

    @Test
    public void noServiceProvided() throws Exception {
        final MockRequestContext mockRequestContext = new MockRequestContext();
        final Event event = this.serviceAuthorizationCheck.doExecute(mockRequestContext);
        assertEquals("success", event.getId());
    }

    @Test
    public void authorizedServiceProvided() throws Exception {
        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.getFlowScope().put("service", this.authorizedService);
        final Event event = this.serviceAuthorizationCheck.doExecute(mockRequestContext);
        assertEquals("success", event.getId());
    }

    @Test
    public void unauthorizedServiceProvided() throws Exception {
        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.getFlowScope().put("service", this.unauthorizedService);

        this.thrown.expect(UnauthorizedServiceException.class);
        this.thrown.expectMessage("Service Management: Unauthorized Service Access. Service [null] is not allowed access via the service registry.");

        this.serviceAuthorizationCheck.doExecute(mockRequestContext);
        fail("Should have thrown UnauthorizedServiceException");
    }

    @Test
    public void serviceThatIsNotRegisteredProvided() throws Exception {
        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.getFlowScope().put("service", this.undefinedService);

        this.thrown.expect(UnauthorizedServiceException.class);
        this.thrown.expectMessage("Service Management: missing service. Service [null] is not found in service registry.");

        this.serviceAuthorizationCheck.doExecute(mockRequestContext);
        fail("Should have thrown UnauthorizedServiceException");
    }
}
