package org.apereo.cas;

import org.apereo.cas.web.flow.AuthenticationViaFormActionTests;
import org.apereo.cas.web.flow.CreateTicketGrantingTicketActionTests;
import org.apereo.cas.web.flow.FlowExecutionExceptionResolverTests;
import org.apereo.cas.web.flow.FrontChannelLogoutActionTests;
import org.apereo.cas.web.flow.GatewayServicesManagementCheckActionTests;
import org.apereo.cas.web.flow.GenerateServiceTicketActionTests;
import org.apereo.cas.web.flow.GenericSuccessViewActionTests;
import org.apereo.cas.web.flow.InitialFlowSetupActionCookieTests;
import org.apereo.cas.web.flow.InitialFlowSetupActionTests;
import org.apereo.cas.web.flow.LogoutActionTests;
import org.apereo.cas.web.flow.SendTicketGrantingTicketActionSsoTests;
import org.apereo.cas.web.flow.SendTicketGrantingTicketActionTests;
import org.apereo.cas.web.flow.ServiceAuthorizationCheckActionMockitoTests;
import org.apereo.cas.web.flow.ServiceAuthorizationCheckActionTests;
import org.apereo.cas.web.flow.ServiceWarningActionTests;
import org.apereo.cas.web.flow.SetServiceUnauthorizedRedirectUrlActionTests;
import org.apereo.cas.web.flow.TerminateSessionActionTests;
import org.apereo.cas.web.flow.TerminateSessionConfirmingActionTests;
import org.apereo.cas.web.flow.TicketGrantingTicketCheckActionTests;
import org.apereo.cas.web.flow.VerifyRequiredServiceActionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllActionsTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    AuthenticationViaFormActionTests.class,
    FrontChannelLogoutActionTests.class,
    GenerateServiceTicketActionTests.class,
    GenericSuccessViewActionTests.class,
    InitialFlowSetupActionTests.class,
    LogoutActionTests.class,
    FlowExecutionExceptionResolverTests.class,
    InitialFlowSetupActionCookieTests.class,
    SendTicketGrantingTicketActionTests.class,
    SendTicketGrantingTicketActionSsoTests.class,
    ServiceAuthorizationCheckActionMockitoTests.class,
    CreateTicketGrantingTicketActionTests.class,
    TicketGrantingTicketCheckActionTests.class,
    ServiceWarningActionTests.class,
    TerminateSessionActionTests.class,
    VerifyRequiredServiceActionTests.class,
    SetServiceUnauthorizedRedirectUrlActionTests.class,
    TerminateSessionConfirmingActionTests.class,
    GatewayServicesManagementCheckActionTests.class,
    ServiceAuthorizationCheckActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllActionsTestsSuite {
}
