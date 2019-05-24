package org.apereo.cas.mfa.accepto;

import org.apereo.cas.mfa.accepto.web.flow.AccepttoMultifactorFetchChannelActionTests;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoMultifactorValidateChannelActionTests;
import org.apereo.cas.mfa.accepto.web.flow.qr.AccepttoQRCodeAuthenticationHandlerTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllAccepttoTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    AccepttoMultifactorAuthenticationHandlerTests.class,
    AccepttoQRCodeAuthenticationHandlerTests.class,
    AccepttoMultifactorFetchChannelActionTests.class,
    AccepttoMultifactorValidateChannelActionTests.class
})
public class AllAccepttoTestsSuite {
}
