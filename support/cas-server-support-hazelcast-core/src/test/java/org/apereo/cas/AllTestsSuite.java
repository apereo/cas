package org.apereo.cas;

import org.apereo.cas.hz.HazelcastBannerContributorTests;
import org.apereo.cas.hz.HazelcastConfigurationFactoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    HazelcastConfigurationFactoryTests.class,
    HazelcastBannerContributorTests.class
})
@Suite
public class AllTestsSuite {
}
