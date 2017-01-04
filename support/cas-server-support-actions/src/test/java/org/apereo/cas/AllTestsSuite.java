package org.apereo.cas;

import org.apereo.cas.web.flow.AuthenticationViaFormActionTests;
import org.apereo.cas.web.flow.CasDefaultFlowUrlHandlerTests;
import org.apereo.cas.web.flow.FrontChannelLogoutActionTests;
import org.apereo.cas.web.flow.GenerateServiceTicketActionTests;
import org.apereo.cas.web.flow.GenericSuccessViewActionTests;
import org.apereo.cas.web.flow.InitialFlowSetupActionCookieTests;
import org.apereo.cas.web.flow.InitialFlowSetupActionSsoTests;
import org.apereo.cas.web.flow.InitialFlowSetupActionTests;
import org.apereo.cas.web.flow.LogoutActionTests;
import org.apereo.cas.web.flow.SendTicketGrantingTicketActionTests;
import org.apereo.cas.web.flow.ServiceAuthorizationCheckTests;
import org.apereo.cas.web.flow.TicketGrantingTicketCheckActionTests;
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
        ServiceAuthorizationCheckTests.class,
        TicketGrantingTicketCheckActionTests.class, 
        CasDefaultFlowUrlHandlerTests.class
})
public class AllTestsSuite {
}
