package org.apereo.cas.web.flow.action;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link SurrogateTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    SurrogateInitialAuthenticationActionTests.class,
    SurrogateSelectionActionTests.class,
    SurrogateAuthorizationActionTests.class,
    LoadSurrogatesListActionTests.class
})
public class SurrogateTestsSuite {
}
