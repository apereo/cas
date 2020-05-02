package org.apereo.cas;

import org.apereo.cas.support.saml.SamlAssertionRealmCodecTests;
import org.apereo.cas.support.validation.CipheredCredentialsValidatorTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @since 6.1.0
 */
@SelectClasses({
    SamlAssertionRealmCodecTests.class,
    CipheredCredentialsValidatorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
