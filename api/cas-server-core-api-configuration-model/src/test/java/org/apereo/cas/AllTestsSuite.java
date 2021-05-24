package org.apereo.cas;

import org.apereo.cas.configuration.AdditionalMetadataVerificationTests;
import org.apereo.cas.configuration.model.support.syncope.SyncopeAuthenticationPropertiesTests;
import org.apereo.cas.configuration.support.BeansTests;
import org.apereo.cas.configuration.support.CasFeatureModuleTests;
import org.apereo.cas.configuration.support.JasyptEncryptionParametersTests;
import org.apereo.cas.configuration.support.RelaxedPropertyNamesTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    AdditionalMetadataVerificationTests.class,
    JasyptEncryptionParametersTests.class,
    BeansTests.class,
    CasFeatureModuleTests.class,
    SyncopeAuthenticationPropertiesTests.class,
    RelaxedPropertyNamesTests.class
})
@Suite
public class AllTestsSuite {
}
