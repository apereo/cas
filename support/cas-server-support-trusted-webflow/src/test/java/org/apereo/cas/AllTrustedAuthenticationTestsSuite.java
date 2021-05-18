
package org.apereo.cas;

import org.apereo.cas.web.flow.TrustedAuthenticationWebflowConfigurerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTrustedAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses(TrustedAuthenticationWebflowConfigurerTests.class)
@Suite
public class AllTrustedAuthenticationTestsSuite {
}
