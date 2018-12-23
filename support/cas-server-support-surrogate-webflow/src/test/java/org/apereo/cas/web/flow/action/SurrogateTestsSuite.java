package org.apereo.cas.web.flow.action;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link SurrogateTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SurrogateInitialAuthenticationActionTests.class,
    SurrogateSelectionActionTests.class,
    SurrogateAuthorizationActionTests.class,
    LoadSurrogatesListActionTests.class
})
public class SurrogateTestsSuite {
}
