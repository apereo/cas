package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.trigger.AdaptiveMultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AdaptiveMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AdaptiveMultifactorAuthenticationTriggerTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private GeoLocationService geoLocationService;
    private Authentication authentication;
    private RegisteredService registeredService;
    private MockHttpServletRequest httpRequest;

    @BeforeEach
    public void setup() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        this.authentication = mock(Authentication.class);
        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn("casuser");
        when(authentication.getPrincipal()).thenReturn(principal);

        this.registeredService = mock(RegisteredService.class);
        when(registeredService.getName()).thenReturn("Service");
        when(registeredService.getServiceId()).thenReturn("http://app.org");
        when(registeredService.getId()).thenReturn(1000L);

        this.geoLocationService = mock(GeoLocationService.class);

        this.httpRequest = new MockHttpServletRequest();
        this.httpRequest.setRemoteAddr("185.86.151.11");
        this.httpRequest.setLocalAddr("185.88.151.12");
        this.httpRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Windows NT 10.0; WOW64)");
        ClientInfoHolder.setClientInfo(new ClientInfo(this.httpRequest));
    }

    @Test
    public void verifyOperationByRequestIP() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getAdaptive().getRequireMultifactor().put("mfa-dummy", "185.86.151.11");
        val trigger = new AdaptiveMultifactorAuthenticationTrigger(this.geoLocationService, props);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    public void verifyOperationByRequestUserAgent() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getAdaptive().getRequireMultifactor().put("mfa-dummy", "^Mozilla.+");
        val trigger = new AdaptiveMultifactorAuthenticationTrigger(this.geoLocationService, props);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    public void verifyOperationByRequestGeoLocation() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getAdaptive().getRequireMultifactor().put("mfa-dummy", ".+London.+");
        val geoResponse = new GeoLocationResponse();
        geoResponse.addAddress("123 Main St London UK");
        when(this.geoLocationService.locate(anyString(), any(GeoLocationRequest.class))).thenReturn(geoResponse);
        val trigger = new AdaptiveMultifactorAuthenticationTrigger(this.geoLocationService, props);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }
}
