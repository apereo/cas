package org.apereo.cas;

import org.apereo.cas.aup.JdbcAcceptableUsagePolicyRepositoryAdvancedTests;
import org.apereo.cas.aup.JdbcAcceptableUsagePolicyRepositoryTests;

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
    JdbcAcceptableUsagePolicyRepositoryAdvancedTests.class,
    JdbcAcceptableUsagePolicyRepositoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
