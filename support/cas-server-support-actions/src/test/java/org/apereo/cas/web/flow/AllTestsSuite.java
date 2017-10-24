package org.apereo.cas.web.flow;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({AuthenticationViaFormActionTests.class,
        FrontChannelLogoutActionTests.class,
        GenerateServiceTicketActionTests.class,
        GenericSuccessViewActionTests.class,
        InitialFlowSetupActionTests.class,
        LogoutActionTests.class,
        InitialFlowSetupActionSsoTests.class,
        InitialFlowSetupActionCookieTests.class,
        SendTicketGrantingTicketActionTests.class,
        SendTicketGrantingTicketActionSsoTests.class,
        ServiceAuthorizationCheckTests.class,
        TicketGrantingTicketCheckActionTests.class
})
public class AllTestsSuite {
}
