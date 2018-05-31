package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * This is {@link MultifactorAuthenticationVerifyTrustActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(locations = "classpath:trusted-mfa-webflow.properties")
public class MultifactorAuthenticationVerifyTrustActionTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyDeviceNotTrusted() throws Exception {
        final var r = getMultifactorAuthenticationTrustRecord();
        r.setRecordDate(LocalDateTime.now().minusSeconds(5));
        mfaTrustEngine.set(r);

        final var context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(r.getPrincipal()), context);
        assertEquals("no", mfaVerifyTrustAction.execute(context).getId());
    }

    @Test
    public void verifyDeviceTrusted() throws Exception {
        final var context = new MockRequestContext();

        final var request = new MockHttpServletRequest();
        request.setRemoteAddr("123.456.789.000");
        request.setLocalAddr("123.456.789.000");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        final var response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        final var r = getMultifactorAuthenticationTrustRecord();
        r.setRecordDate(LocalDateTime.now().minusSeconds(5));
        r.setDeviceFingerprint(deviceFingerprintStrategy.determineFingerprint(r.getPrincipal(), context, true));
        mfaTrustEngine.set(r);

        assertNotNull(response.getCookies());
        assertTrue(response.getCookies().length == 1);
        request.setCookies(response.getCookies());

        final var authn = CoreAuthenticationTestUtils.getAuthentication(r.getPrincipal());
        WebUtils.putAuthentication(authn, context);
        assertEquals("yes", mfaVerifyTrustAction.execute(context).getId());

        assertTrue(MultifactorAuthenticationTrustUtils.isMultifactorAuthenticationTrustedInScope(context));
        assertTrue(authn.getAttributes().containsKey(casProperties.getAuthn().getMfa().getTrusted().getAuthenticationContextAttribute()));
    }
}
