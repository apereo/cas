package org.apereo.cas.trusted.authentication.storage.fingerprint;

import module java.base;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.trusted.web.flow.fingerprint.BrowserDeviceFingerprintExtractor;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BrowserDeviceFingerprintExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("MFATrustedDevices")
class BrowserDeviceFingerprintExtractorTests {
    private static final String FINGERPRINT = "b6f1a7c2d93e";

    @Test
    void verifyFingerprintOfLoginFormIsUsedForLaterRequests() throws Throwable {
        val context = MockRequestContext.create();
        context.setParameter("deviceFingerprint", FINGERPRINT);
        context.setClientInfo();
        BrowserDeviceFingerprintExtractor.rememberDeviceFingerprint(context);
        assertEquals(FINGERPRINT, context.getFlowScope().get(BrowserDeviceFingerprintExtractor.FLOW_SCOPE_ATTRIBUTE_BROWSER_FINGERPRINT));

        ClientInfoHolder.setClientInfo(ClientInfo.from(new MockHttpServletRequest()));
        val extractor = new BrowserDeviceFingerprintExtractor();
        assertEquals(FINGERPRINT, extractor.extract(RegisteredServiceTestUtils.getAuthentication(),
            context.getHttpServletRequest(), context.getHttpServletResponse()).orElseThrow());
    }

    @Test
    void verifyFingerprintOfCurrentRequestWins() throws Throwable {
        val context = MockRequestContext.create();
        context.getFlowScope().put(BrowserDeviceFingerprintExtractor.FLOW_SCOPE_ATTRIBUTE_BROWSER_FINGERPRINT, FINGERPRINT);
        context.setParameter("deviceFingerprint", "a0c4e9b2f17d");
        context.setClientInfo();
        val extractor = new BrowserDeviceFingerprintExtractor();
        assertEquals("a0c4e9b2f17d", extractor.extract(RegisteredServiceTestUtils.getAuthentication(),
            context.getHttpServletRequest(), context.getHttpServletResponse()).orElseThrow());
    }

    @Test
    void verifyNoFingerprintCollected() throws Throwable {
        val context = MockRequestContext.create();
        context.setClientInfo();
        BrowserDeviceFingerprintExtractor.rememberDeviceFingerprint(context);
        assertFalse(context.getFlowScope().contains(BrowserDeviceFingerprintExtractor.FLOW_SCOPE_ATTRIBUTE_BROWSER_FINGERPRINT));
        val extractor = new BrowserDeviceFingerprintExtractor();
        assertEquals(ClientInfo.UNKNOWN, extractor.extract(RegisteredServiceTestUtils.getAuthentication(),
            context.getHttpServletRequest(), context.getHttpServletResponse()).orElseThrow());
    }
}
