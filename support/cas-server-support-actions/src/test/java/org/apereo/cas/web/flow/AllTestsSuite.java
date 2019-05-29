package org.apereo.cas.web.flow;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
    VerifyRequiredServiceActionTests.class,
    SetServiceUnauthorizedRedirectUrlActionTests.class,
    TerminateSessionConfirmingActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
