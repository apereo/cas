package org.apereo.cas;

import org.apereo.cas.aup.ConditionalOnExpressionNegativeTests;
import org.apereo.cas.aup.RedisAcceptableUsagePolicyRepositoryTests;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link RedisAcceptableUsageTestsSuite}.
 *
 * @author Hal Deadman
 * @since 6.4.0
 */
@SelectClasses({
    ConditionalOnExpressionNegativeTests.class,
    RedisAcceptableUsagePolicyRepositoryTests.class
})
@RunWith(JUnitPlatform.class)
public class RedisAcceptableUsageTestsSuite {
}
