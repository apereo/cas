package org.apereo.cas;


import org.apereo.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandlerTests;
import org.apereo.cas.support.pac4j.web.flow.DelegatedClientAuthenticationActionTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({ClientAuthenticationHandlerTests.class, DelegatedClientAuthenticationActionTests.class})
public class AllTestsSuite {
}
