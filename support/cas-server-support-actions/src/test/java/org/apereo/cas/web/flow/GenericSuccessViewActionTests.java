package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.login.GenericSuccessViewAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GenericSuccessViewAction}
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("WebflowActions")
class GenericSuccessViewActionTests {

    @Nested
    @TestPropertySource(properties = "cas.view.authorized-services-on-successful-login=true")
    class DefaultTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_GENERIC_SUCCESS_VIEW)
        private Action genericSuccessViewAction;

        @Test
        void verifyAuthzServices() throws Throwable {
            val registeredService1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), Map.of());
            getServicesManager().save(registeredService1);

            val registeredService2 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            registeredService2.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
            getServicesManager().save(registeredService2);

            val context = MockRequestContext.create(applicationContext);
            val tgt = new MockTicketGrantingTicket("casuser");
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            getTicketRegistry().addTicket(tgt);

            val result = genericSuccessViewAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
            assertNotNull(WebUtils.getAuthorizedServices(context));
            val list = WebUtils.getAuthorizedServices(context);
            assertFalse(list.isEmpty());
        }


        @Test
        void verifyAuthn() throws Throwable {
            val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://github.com/apereo/cas");
            getServicesManager().save(registeredService);
            val context = MockRequestContext.create(applicationContext);

            val tgt = new MockTicketGrantingTicket(CoreAuthenticationTestUtils.getAuthentication());
            getTicketRegistry().addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            val result = genericSuccessViewAction.execute(context);

            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
            assertNotNull(WebUtils.getAuthentication(context));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.view.authorized-services-on-successful-login=true",
        "cas.view.default-redirect-url=https://github.com/apereo/cas"
    })
    class RedirectTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_GENERIC_SUCCESS_VIEW)
        private Action genericSuccessViewAction;

        @Test
        void verifyRedirect() throws Throwable {
            val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://github.com/apereo/cas");
            getServicesManager().save(registeredService);
            val context = MockRequestContext.create(applicationContext);
            val result = genericSuccessViewAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
            assertTrue(context.getMockExternalContext().getExternalRedirectRequested());
            assertEquals(casProperties.getView().getDefaultRedirectUrl(), context.getMockExternalContext().getExternalRedirectUrl());
        }
    }
}
