package org.apereo.cas.trusted.authentication.storage.fingerprint;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.util.MockRequestContext;
import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDeviceFingerprintStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class,
    properties = "cas.authn.mfa.trusted.device-fingerprint.browser.enabled=true")
@Tag("MFATrustedDevices")
@ExtendWith(CasTestExtension.class)
class DefaultDeviceFingerprintStrategyTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val request = context.getHttpServletRequest();
        request.setRemoteAddr("123.456.789.000");
        request.setLocalAddr("123.456.789.000");
        context.withUserAgent();
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));

        val authentication = RegisteredServiceTestUtils.getAuthentication();
        val f1 = deviceFingerprintStrategy.determineFingerprint(authentication, request, context.getHttpServletResponse());
        request.setCookies(context.getHttpServletResponse().getCookies());
        val f2 = deviceFingerprintStrategy.determineFingerprint(authentication, request, context.getHttpServletResponse());
        request.setCookies(context.getHttpServletResponse().getCookies());
        assertEquals(f1, f2);

        val f3 = deviceFingerprintStrategy.determineFingerprint(authentication, request, context.getHttpServletResponse());
        assertNotNull(context.getHttpServletResponse().getCookies());
        assertEquals(1, context.getHttpServletResponse().getCookies().length);
        request.setCookies(context.getHttpServletResponse().getCookies());

        val f4 = deviceFingerprintStrategy.determineFingerprint(authentication, request, context.getHttpServletResponse());
        assertEquals(f3, f4);
    }
}
