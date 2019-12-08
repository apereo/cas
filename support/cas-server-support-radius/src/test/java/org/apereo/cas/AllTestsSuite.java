
package org.apereo.cas;

import org.apereo.cas.adaptors.radius.authentication.handler.support.RadiusAuthenticationHandlerTests;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAccessChallengedMultifactorAuthenticationTriggerTests;
import org.apereo.cas.config.RadiusConfigurationTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    RadiusConfigurationTests.class,
    RadiusAccessChallengedMultifactorAuthenticationTriggerTests.class,
    RadiusAuthenticationHandlerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
