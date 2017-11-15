package org.apereo.cas;


import org.apereo.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandlerTests;
import org.apereo.cas.support.pac4j.web.flow.DelegatedClientAuthenticationActionTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.apereo.cas.support.pac4j.web.flow.IgnoreServiceRedirectUrlForSamlActionTests;
import org.apereo.cas.support.pac4j.web.flow.SingleLogoutPreparationActionTests;
import org.apereo.cas.support.pac4j.web.flow.TerminateSessionFlowExecutionListenerTests;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ClientAuthenticationHandlerTests.class,
    DelegatedClientAuthenticationActionTests.class,
    IgnoreServiceRedirectUrlForSamlActionTests.class,
    SingleLogoutPreparationActionTests.class,
    TerminateSessionFlowExecutionListenerTests.class
})
public class AllTestsSuite {
}
