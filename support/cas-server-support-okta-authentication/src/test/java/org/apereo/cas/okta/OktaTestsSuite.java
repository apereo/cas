package org.apereo.cas.okta;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link OktaTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    OktaAuthenticationStateHandlerTests.class,
    OktaAuthenticationStateHandlerAdapterTests.class
})
@RunWith(JUnitPlatform.class)
public class OktaTestsSuite {
}
