package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.BindException;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("WebflowAuthenticationActions")
@ExtendWith(CasTestExtension.class)
class AuthenticationViaFormActionTests extends AbstractWebflowActionsTests {

    private static final String TEST = "test";

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_AUTHENTICATION_VIA_FORM_ACTION)
    private Action action;

    @Autowired
    @Qualifier(CasCookieBuilder.BEAN_NAME_WARN_COOKIE_BUILDER)
    private CasCookieBuilder warnCookieGenerator;

    @BeforeEach
    void beforeEach() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        requestContext
            .setRemoteAddr("127.26.152.11")
            .setLocalAddr("109.98.51.12")
            .withUserAgent()
            .setClientInfo();
    }

    @Test
    void verifySuccessfulAuthenticationWithNoService() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(CasProtocolConstants.PARAMETER_USERNAME, TEST);
        context.setParameter(CasProtocolConstants.PARAMETER_PASSWORD, TEST);
        val credentials = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, credentials);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }

    @Test
    void verifySuccessfulAuthenticationWithNoServiceAndWarn() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        context.setParameter(CasProtocolConstants.PARAMETER_USERNAME, TEST);
        context.setParameter(CasProtocolConstants.PARAMETER_PASSWORD, TEST);
        context.setParameter(CasWebflowConstants.ATTRIBUTE_WARN_ON_REDIRECT, "true");

        val credentials = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, credentials);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }

    @Test
    void verifySuccessfulAuthenticationWithServiceAndWarn() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        context.setParameter(CasProtocolConstants.PARAMETER_USERNAME, TEST);
        context.setParameter(CasProtocolConstants.PARAMETER_PASSWORD, TEST);
        context.setParameter(CasWebflowConstants.ATTRIBUTE_WARN_ON_REDIRECT, "true");
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, TEST);

        val credentials = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, credentials);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        assertNotNull(context.getHttpServletResponse().getCookie(warnCookieGenerator.getCookieName()));
    }

    @Test
    void verifyFailedAuthenticationWithNoService() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        context.setParameter(CasProtocolConstants.PARAMETER_USERNAME, TEST);
        context.setParameter(CasProtocolConstants.PARAMETER_PASSWORD, "test2");

        val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword();
        putCredentialInRequestScope(context, credential);

        context.getRequestScope().put("org.springframework.validation.BindException.credentials", new BindException(credential, "credential"));
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, action.execute(context).getId());
    }

    @Test
    void verifyRenewWithServiceAndSameCredentials() throws Throwable {
        val credentials = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val service = RegisteredServiceTestUtils.getService(RegisteredServiceTestUtils.CONST_TEST_URL);
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(
            getAuthenticationSystemSupport(), service, credentials);

        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);

        context.setParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE,
            RegisteredServiceTestUtils.getService(RegisteredServiceTestUtils.CONST_TEST_URL).getId());
        putCredentialInRequestScope(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService());
        val ev = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, ev.getId());
    }

    @Test
    void verifyRenewWithServiceAndDifferentCredentials() throws Throwable {
        val credentials = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();

        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(
            getAuthenticationSystemSupport(), RegisteredServiceTestUtils.getService(TEST), credentials);

        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val context = MockRequestContext.create(applicationContext);

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        context.setParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(TEST).getId());

        val c2 = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c2);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }

    @Test
    void verifyServiceTicketCreationWithSso() throws Throwable {
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val authResult = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            RegisteredServiceTestUtils.getService(TEST), credential);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(authResult);

        val context = MockRequestContext.create(applicationContext);

        putCredentialInRequestScope(context, credential);
        WebUtils.putAuthenticationResult(authResult, context);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(TEST));
        WebUtils.putAuthentication(authResult.getAuthentication(), context);
        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);

        assertEquals(CasWebflowConstants.TRANSITION_ID_GENERATE_SERVICE_TICKET, action.execute(context).getId());
    }

    @Test
    void verifyRenewWithServiceAndBadCredentials() throws Throwable {
        val credentials = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val service = RegisteredServiceTestUtils.getService(TEST);
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(
            getAuthenticationSystemSupport(), service, credentials);

        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        val context = MockRequestContext.create(applicationContext);

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        context.setParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

        val c2 = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword();
        putCredentialInRequestScope(context, c2);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, action.execute(context).getId());
    }

    private static void putCredentialInRequestScope(final RequestContext context,
                                                    final Credential credential) {
        WebUtils.putCredential(context, credential);
    }
}
