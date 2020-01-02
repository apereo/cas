
package org.apereo.cas;

import org.apereo.cas.grouper.services.GrouperRegisteredServiceAccessStrategyTests;
import org.apereo.cas.web.flow.GrouperMultifactorAuthenticationPolicyEventResolverTests;

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
    GrouperMultifactorAuthenticationPolicyEventResolverTests.class,
    GrouperRegisteredServiceAccessStrategyTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
