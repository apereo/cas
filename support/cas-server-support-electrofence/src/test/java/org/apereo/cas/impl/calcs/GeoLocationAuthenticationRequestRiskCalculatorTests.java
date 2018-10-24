package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * This is {@link GeoLocationAuthenticationRequestRiskCalculatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@TestPropertySource(properties = {"cas.authn.adaptive.risk.geoLocation.enabled=true", "cas.googleMaps.ipStackApiAccessKey=6bde37c76ad15c8a5c828fafad8b0bc4"})
public class GeoLocationAuthenticationRequestRiskCalculatorTests extends BaseAuthenticationRequestRiskCalculatorTests {

    @Test
    public void verifyTestWhenNoAuthnEventsFoundForUser() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("geoperson");
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        val score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isHighestRisk());
    }

    @Test
    public void verifyTestWhenAuthnEventsFoundForUser() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("172.217.11.174");
        request.setLocalAddr("127.0.0.1");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        val score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isHighestRisk());
    }
}
