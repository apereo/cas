package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.impl.mock.MockTicketGrantingTicketCreatedEventProducer;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import javax.net.ssl.HttpsURLConnection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GeoLocationAuthenticationRequestRiskCalculatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Import(GeoLocationAuthenticationRequestRiskCalculatorTests.GeoLocationServiceTestConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.adaptive.risk.geo-location.enabled=true",
    "cas.google-maps.ip-stack-api-access-key=6bde37c76ad15c8a5c828fafad8b0bc4"
})
@Tag("Authentication")
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
    public void verifyTestWithGeoLoc() {
        val id = UUID.randomUUID().toString();
        MockTicketGrantingTicketCreatedEventProducer.createEvent(id, casEventRepository);
        val authentication = CoreAuthenticationTestUtils.getAuthentication(id);
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        request.setParameter("geolocation", "40,70,1000,100");
        val score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isHighestRisk());
    }

    @Test
    public void verifyTestWhenAuthnEventsFoundForUser() {
        HttpsURLConnection.setDefaultHostnameVerifier(CasSSLContext.disabled().getHostnameVerifier());
        HttpsURLConnection.setDefaultSSLSocketFactory(CasSSLContext.disabled().getSslContext().getSocketFactory());

        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("172.217.11.174");
        request.setLocalAddr("127.0.0.1");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        val score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isHighestRisk());
    }

    @TestConfiguration("GeoLocationServiceTestConfiguration")
    public static class GeoLocationServiceTestConfiguration {
        @Bean
        public GeoLocationService geoLocationService() {
            val service = mock(GeoLocationService.class);
            val response = new GeoLocationResponse();
            response.addAddress("MSIE");
            when(service.locate(anyString(), any(GeoLocationRequest.class))).thenReturn(response);
            return service;
        }
    }
}
