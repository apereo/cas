package org.apereo.cas.web;

import org.apereo.cas.web.flow.DelegatedClientAuthenticationActionTests;
import org.apereo.cas.web.flow.IgnoreServiceRedirectUrlForSamlActionTests;
import org.apereo.cas.web.flow.LimitedTerminateSessionActionTests;
import org.apereo.cas.web.flow.SingleLogoutPreparationActionTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import lombok.extern.slf4j.Slf4j;


/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DelegatedClientAuthenticationActionTests.class,
    IgnoreServiceRedirectUrlForSamlActionTests.class,
    SingleLogoutPreparationActionTests.class,
    LimitedTerminateSessionActionTests.class
})
@Slf4j
public class AllTestsSuite {
}
