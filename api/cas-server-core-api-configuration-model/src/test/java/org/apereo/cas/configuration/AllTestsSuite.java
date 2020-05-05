package org.apereo.cas.configuration;

import org.apereo.cas.configuration.support.RelaxedPropertyNamesTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    AdditionalMetadataVerificationTests.class,
    RelaxedPropertyNamesTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
