package org.apereo.cas;

import org.apereo.cas.aup.JdbcAcceptableUsagePolicyRepositoryAdvancedTests;
import org.apereo.cas.aup.JdbcAcceptableUsagePolicyRepositoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
@Suite
public class AllTestsSuite {
}
