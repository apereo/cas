package org.apereo.cas;

import org.apereo.cas.config.CoreSamlConfigurationTests;
import org.apereo.cas.support.saml.SamlUtilsTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllSamlTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    CoreSamlConfigurationTests.class,
    SamlUtilsTests.class
})
@RunWith(JUnitPlatform.class)
public class AllSamlTestsSuite {
}
