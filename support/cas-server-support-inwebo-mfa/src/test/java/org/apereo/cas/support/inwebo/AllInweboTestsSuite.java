package org.apereo.cas.support.inwebo;

import org.apereo.cas.support.inwebo.web.flow.actions.InweboCheckAuthenticationActionTests;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboCheckUserActionTests;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboMustEnrollActionTests;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboPushAuthenticateActionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is all the Inwebo tests.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@SelectClasses({
        InweboCheckAuthenticationActionTests.class,
        InweboCheckUserActionTests.class,
        InweboMustEnrollActionTests.class,
        InweboPushAuthenticateActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllInweboTestsSuite {
}
