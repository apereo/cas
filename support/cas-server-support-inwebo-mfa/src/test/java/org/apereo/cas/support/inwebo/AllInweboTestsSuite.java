package org.apereo.cas.support.inwebo;

import org.apereo.cas.support.inwebo.web.flow.actions.CheckAuthenticationActionTests;
import org.apereo.cas.support.inwebo.web.flow.actions.CheckUserActionTests;
import org.apereo.cas.support.inwebo.web.flow.actions.MustEnrollActionTests;
import org.apereo.cas.support.inwebo.web.flow.actions.PushAuthenticateActionTests;

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
        CheckAuthenticationActionTests.class,
        CheckUserActionTests.class,
        MustEnrollActionTests.class,
        PushAuthenticateActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllInweboTestsSuite {
}
