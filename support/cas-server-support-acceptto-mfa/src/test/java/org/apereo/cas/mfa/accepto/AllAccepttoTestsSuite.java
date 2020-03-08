package org.apereo.cas.mfa.accepto;

import org.apereo.cas.mfa.accepto.web.flow.AccepttoMultifactorDetermineUserAccountStatusActionTests;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoMultifactorFetchChannelActionTests;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoMultifactorValidateChannelActionTests;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoMultifactorValidateUserDeviceRegistrationActionTests;
import org.apereo.cas.mfa.accepto.web.flow.qr.AccepttoQRCodeAuthenticationHandlerTests;
import org.apereo.cas.mfa.accepto.web.flow.qr.AccepttoQRCodeValidateWebSocketChannelActionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllAccepttoTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    AccepttoMultifactorAuthenticationHandlerTests.class,
    AccepttoQRCodeAuthenticationHandlerTests.class,
    AccepttoApiUtilsTests.class,
    AccepttoQRCodeValidateWebSocketChannelActionTests.class,
    AccepttoMultifactorDetermineUserAccountStatusActionTests.class,
    AccepttoMultifactorValidateUserDeviceRegistrationActionTests.class,
    AccepttoMultifactorFetchChannelActionTests.class,
    AccepttoMultifactorValidateChannelActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllAccepttoTestsSuite {
}
