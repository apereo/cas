
package org.apereo.cas;

import org.apereo.cas.config.CasPersonDirectoryGrouperConfigurationTests;
import org.apereo.cas.grouper.GrouperPersonAttributeDaoTests;
import org.apereo.cas.grouper.services.GrouperRegisteredServiceAccessStrategyTests;
import org.apereo.cas.web.flow.GrouperMultifactorAuthenticationPolicyEventResolverTests;
import org.apereo.cas.web.flow.GrouperMultifactorAuthenticationTriggerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    GrouperPersonAttributeDaoTests.class,
    GrouperMultifactorAuthenticationTriggerTests.class,
    GrouperMultifactorAuthenticationPolicyEventResolverTests.class,
    CasPersonDirectoryGrouperConfigurationTests.class,
    GrouperRegisteredServiceAccessStrategyTests.class
})
@Suite
public class AllTestsSuite {
}
