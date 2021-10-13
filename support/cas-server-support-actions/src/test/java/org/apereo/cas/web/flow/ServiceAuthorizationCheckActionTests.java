package org.apereo.cas.web.flow;

import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ServiceAuthorizationCheckActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("WebflowActions")
public class ServiceAuthorizationCheckActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SERVICE_AUTHZ_CHECK)
    private Action action;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @BeforeEach
    public void beforeEach() {
        servicesManager.deleteAll();
    }

    @Test
    public void verifyNoServiceFound() {
        val request = new MockHttpServletRequest();
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("invalid-service-123"));
        assertThrows(UnauthorizedServiceException.class, () -> this.action.execute(context));
    }

    @Test
    public void verifyDisabledServiceFound() throws Exception {
        val svc22 = RegisteredServiceTestUtils.getRegisteredService("cas-access-disabled");
        val strategy = new DefaultRegisteredServiceAccessStrategy();
        strategy.setEnabled(false);
        strategy.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        svc22.setAccessStrategy(strategy);
        servicesManager.save(svc22);

        val request = new MockHttpServletRequest();
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("cas-access-disabled"));
        assertThrows(UnauthorizedServiceException.class, () -> this.action.execute(context));
        assertNotNull(WebUtils.getUnauthorizedRedirectUrlFromFlowScope(context));
    }

    @Test
    public void verifyExclusiveAuthnDelegationMode() {
        val svc23 = RegisteredServiceTestUtils.getRegisteredService("cas-access-delegation");
        val strategy23 = new DefaultRegisteredServiceAccessStrategy();
        strategy23.setEnabled(true);
        val delegate = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        delegate.setExclusive(true);
        strategy23.setDelegatedAuthenticationPolicy(delegate);
        svc23.setAccessStrategy(strategy23);
        servicesManager.save(svc23);

        val request = new MockHttpServletRequest();
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("cas-access-delegation"));
        assertDoesNotThrow(() -> this.action.execute(context));
        assertFalse(WebUtils.isCasLoginFormViewable(context));
    }
}
