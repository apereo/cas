package org.apereo.cas.web.flow;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllTestsSuite}.
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
    InitialFlowSetupActionSsoTests.class,
    InitialFlowSetupActionCookieTests.class,
    SendTicketGrantingTicketActionTests.class,
    SendTicketGrantingTicketActionSsoTests.class,
    ServiceAuthorizationCheckTests.class,
    CreateTicketGrantingTicketActionTests.class,
    TicketGrantingTicketCheckActionTests.class,
    ServiceWarningActionTests.class,
    TerminateSessionActionTests.class,
    SetServiceUnauthorizedRedirectUrlActionTests.class,
    TerminateSessionConfirmingActionTests.class
})
public class AllTestsSuite {
}
