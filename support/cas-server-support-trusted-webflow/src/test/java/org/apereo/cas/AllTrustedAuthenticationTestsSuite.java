
package org.apereo.cas;

import org.apereo.cas.web.flow.TrustedAuthenticationWebflowConfigurerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTrustedAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses(TrustedAuthenticationWebflowConfigurerTests.class)
@RunWith(JUnitPlatform.class)
public class AllTrustedAuthenticationTestsSuite {
}
