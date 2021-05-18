package org.apereo.cas;

import org.apereo.cas.qr.authentication.JsonResourceQRAuthenticationDeviceRepositoryTests;
import org.apereo.cas.qr.authentication.QRAuthenticationDeviceRepositoryTests;
import org.apereo.cas.qr.authentication.QRAuthenticationTokenAuthenticationHandlerTests;
import org.apereo.cas.qr.authentication.QRAuthenticationTokenCredentialTests;
import org.apereo.cas.qr.validation.DefaultQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.qr.validation.QRAuthenticationTokenValidationResultTests;
import org.apereo.cas.qr.web.QRAuthenticationChannelControllerTests;
import org.apereo.cas.qr.web.QRAuthenticationDeviceRepositoryEndpointTests;
import org.apereo.cas.qr.web.flow.QRAuthenticationGenerateCodeActionTests;
import org.apereo.cas.qr.web.flow.QRAuthenticationValidateTokenActionTests;
import org.apereo.cas.qr.web.flow.QRAuthenticationWebflowConfigurerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    QRAuthenticationTokenCredentialTests.class,
    QRAuthenticationChannelControllerTests.class,
    QRAuthenticationTokenAuthenticationHandlerTests.class,
    QRAuthenticationValidateTokenActionTests.class,
    QRAuthenticationGenerateCodeActionTests.class,
    QRAuthenticationWebflowConfigurerTests.class,
    QRAuthenticationDeviceRepositoryEndpointTests.class,
    QRAuthenticationDeviceRepositoryTests.class,
    DefaultQRAuthenticationTokenValidatorServiceTests.class,
    QRAuthenticationTokenValidationResultTests.class,
    JsonResourceQRAuthenticationDeviceRepositoryTests.class
})
@Suite
public class AllTestsSuite {
}
