package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationVerifyTrustActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class)
@Getter
@Tag("Webflow")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class MultifactorAuthenticationVerifyTrustActionTests extends AbstractMultifactorAuthenticationTrustStorageTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    @Order(1)
    public void verifyDeviceNotTrusted() throws Exception {
        val r = getMultifactorAuthenticationTrustRecord();
        r.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(5));
        getMfaTrustEngine().save(r);

        val context = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService("sample-service", Collections.EMPTY_MAP));
        context.setExternalContext(new ServletExternalContext(new MockServletContext(),
            new MockHttpServletRequest(), new MockHttpServletResponse()));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(r.getPrincipal()), context);
        assertEquals("no", mfaVerifyTrustAction.execute(context).getId());
    }

    @Test
    @Order(2)
    public void verifyDeviceTrusted() throws Exception {
        val context = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService("sample-service", Collections.EMPTY_MAP));

        val request = new MockHttpServletRequest();
        request.setRemoteAddr("123.456.789.000");
        request.setLocalAddr("123.456.789.000");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val record = getMultifactorAuthenticationTrustRecord();
        record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(5));
        val deviceFingerprint = deviceFingerprintStrategy.determineFingerprint(record.getPrincipal(), context, true);
        record.setDeviceFingerprint(deviceFingerprint);
        mfaTrustEngine.save(record);

        assertNotNull(response.getCookies());
        assertTrue(response.getCookies().length == 1);
        request.setCookies(response.getCookies());

        val authn = RegisteredServiceTestUtils.getAuthentication(record.getPrincipal());
        WebUtils.putAuthentication(authn, context);
        assertEquals("yes", mfaVerifyTrustAction.execute(context).getId());

        assertTrue(MultifactorAuthenticationTrustUtils.isMultifactorAuthenticationTrustedInScope(context));
        assertTrue(authn.getAttributes().containsKey(casProperties.getAuthn().getMfa().getTrusted().getAuthenticationContextAttribute()));
    }

    @BeforeEach
    public void emptyTrustEngine() {
        mfaTrustEngine.getAll().forEach(r -> getMfaTrustEngine().remove(r.getRecordKey()));
    }

}
