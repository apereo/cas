package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AdaptiveMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFA")
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdaptiveMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {

    @Test
    @Order(0)
    @Tag("DisableProviderRegistration")
    public void verifyNoProviders() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getAdaptive().getRequireMultifactor().put("mfa-dummy", ".+London.+");
        val trigger = new AdaptiveMultifactorAuthenticationTrigger(null, props, this.applicationContext);
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class)));
    }

    @Test
    @Order(1)
    public void verifyOperationByRequestIP() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getAdaptive().getRequireMultifactor().put("mfa-dummy", "185.86.151.11");
        val trigger = new AdaptiveMultifactorAuthenticationTrigger(this.geoLocationService, props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    @Order(2)
    public void verifyOperationByRequestUserAgent() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getAdaptive().getRequireMultifactor().put("mfa-dummy", "^Mozilla.+");
        val trigger = new AdaptiveMultifactorAuthenticationTrigger(this.geoLocationService, props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    @Order(3)
    public void verifyOperationByRequestGeoLocation() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getAdaptive().getRequireMultifactor().put("mfa-dummy", ".+London.+");
        val geoResponse = new GeoLocationResponse();
        geoResponse.addAddress("123 Main St London UK");
        when(this.geoLocationService.locate(anyString(), any(GeoLocationRequest.class))).thenReturn(geoResponse);
        val trigger = new AdaptiveMultifactorAuthenticationTrigger(this.geoLocationService, props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }



    @Test
    @Order(5)
    public void verifyMissingProviders() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getAdaptive().getRequireMultifactor().put("mfa-xyz", ".+London.+");
        val trigger = new AdaptiveMultifactorAuthenticationTrigger(null, props, this.applicationContext);
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class)));
    }


    @Test
    @Order(7)
    public void verifyNoLocation() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getAdaptive().getRequireMultifactor().put("mfa-dummy", ".+London.+");
        val trigger = new AdaptiveMultifactorAuthenticationTrigger(this.geoLocationService, props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isEmpty());
    }
}
