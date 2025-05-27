package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationVerifiedEvent;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.util.DateTimeUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import java.time.Clock;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link IpAddressAuthenticationRequestRiskCalculatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@TestPropertySource(properties = "cas.authn.adaptive.risk.ip.enabled=true")
@Tag("Authentication")
class IpAddressAuthenticationRequestRiskCalculatorTests extends BaseAuthenticationRequestRiskCalculatorTests {

    @Test
    void verifyTestWhenNoAuthnEventsFoundForUser() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("nobody");
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        val score = authenticationRiskEvaluator.evaluate(authentication, service, ClientInfo.from(request));
        assertTrue(score.isHighestRisk());
    }

    @Test
    void verifyTestWhenAuthnEventsFoundForUser() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("107.181.69.221");
        request.setLocalAddr("127.0.0.1");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        val score = authenticationRiskEvaluator.evaluate(authentication, service, ClientInfo.from(request));
        assertTrue(authenticationRiskEvaluator.isRiskyAuthenticationScore(score, authentication, service));
    }

    @Test
    void verifyTestWhenAuthenticationRiskVerified() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("157.181.99.223");
        request.setLocalAddr("227.4.6.1");
        request.addHeader(HttpHeaders.USER_AGENT, "Firefox");
        val clientInfo = ClientInfo.from(request);
        ClientInfoHolder.setClientInfo(clientInfo);
        val score = authenticationRiskEvaluator.evaluate(authentication, service, ClientInfo.from(request));

        val event = new CasEvent();
        event.setType(CasRiskyAuthenticationVerifiedEvent.class.getCanonicalName());
        val nowInEpoch = Instant.now(Clock.systemUTC()).toEpochMilli();
        event.putTimestamp(nowInEpoch);
        val dt = DateTimeUtils.zonedDateTimeOf(nowInEpoch);
        event.setCreationTime(dt.toInstant());
        event.putClientIpAddress(clientInfo.getClientIpAddress());
        event.putServerIpAddress(clientInfo.getServerIpAddress());
        event.putAgent(clientInfo.getUserAgent());
        event.setPrincipalId(authentication.getPrincipal().getId());
        casEventRepository.save(event);
        assertFalse(authenticationRiskEvaluator.isRiskyAuthenticationScore(score, authentication, service));
    }
}
