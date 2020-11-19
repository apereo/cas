package org.apereo.cas;

import org.apereo.cas.qr.authentication.QRAuthenticationTokenCredentialTests;
import org.apereo.cas.qr.validation.DefaultQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.qr.validation.QRAuthenticationTokenValidationResultTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    QRAuthenticationTokenCredentialTests.class,
    DefaultQRAuthenticationTokenValidatorServiceTests.class,
    QRAuthenticationTokenValidationResultTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
