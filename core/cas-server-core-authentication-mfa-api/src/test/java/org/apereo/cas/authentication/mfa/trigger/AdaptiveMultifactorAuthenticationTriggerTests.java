package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.trigger.AdaptiveMultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AdaptiveMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class AdaptiveMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
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
