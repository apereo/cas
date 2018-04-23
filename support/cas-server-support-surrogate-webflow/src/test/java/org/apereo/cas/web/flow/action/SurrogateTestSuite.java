package org.apereo.cas.web.flow.action;

import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link SurrogateTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SurrogateInitialAuthenticationActionTests.class,
    SurrogateSelectionActionTests.class,
    SurrogateAuthorizationActionTests.class
})
@Slf4j
public class SurrogateTestSuite {
}
