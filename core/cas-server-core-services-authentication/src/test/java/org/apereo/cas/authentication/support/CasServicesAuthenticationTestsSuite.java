package org.apereo.cas.authentication.support;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link CasServicesAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    DefaultCasProtocolAttributeEncoderTests.class,
    ProtocolAttributeEncoderTests.class
})
@RunWith(JUnitPlatform.class)
public class CasServicesAuthenticationTestsSuite {
}
