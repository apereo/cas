package org.jasig.cas.web.flow;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({AuthenticationViaFormActionTests.class, FrontChannelLogoutActionTests.class,
        GenerateServiceTicketActionTests.class, GenericSuccessViewActionTests.class,
        InitialFlowSetupActionTests.class, LogoutActionTests.class,
        SendTicketGrantingTicketActionTests.class, ServiceAuthorizationCheckTests.class,
        TicketGrantingTicketCheckActionTests.class, CasDefaultFlowUrlHandlerTests.class
})
public class AllTestsSuite {
}
