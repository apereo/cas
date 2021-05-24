package org.apereo.cas;

import org.apereo.cas.aup.RestAcceptableUsagePolicyRepositoryTests;
import org.apereo.cas.config.CasAcceptableUsagePolicyRestConfigurationTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    RestAcceptableUsagePolicyRepositoryTests.class,
    CasAcceptableUsagePolicyRestConfigurationTests.class
})
@Suite
public class AllTestsSuite {
}
