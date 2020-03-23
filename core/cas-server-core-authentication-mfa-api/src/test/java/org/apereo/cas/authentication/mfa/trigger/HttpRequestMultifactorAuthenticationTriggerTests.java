package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link HttpRequestMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFA")
public class HttpRequestMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    public void verifyOperationByHeader() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setRequestHeader("mfaPolicy");
        this.httpRequest.addHeader("mfaPolicy", TestMultifactorAuthenticationProvider.ID);
        val trigger = new HttpRequestMultifactorAuthenticationTrigger(props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    public void verifyOperationByParameter() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setRequestParameter("mfaPolicy");
        this.httpRequest.addParameter("mfaPolicy", TestMultifactorAuthenticationProvider.ID);
        val trigger = new HttpRequestMultifactorAuthenticationTrigger(props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    public void verifyOperationByAttribute() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setSessionAttribute("mfaPolicy");
        httpRequest.setAttribute("mfaPolicy", TestMultifactorAuthenticationProvider.ID);
        val trigger = new HttpRequestMultifactorAuthenticationTrigger(props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    public void verifyOperationBySessionAttribute() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setSessionAttribute("mfaPolicy");
        httpRequest.getSession(true).setAttribute("mfaPolicy", TestMultifactorAuthenticationProvider.ID);
        val trigger = new HttpRequestMultifactorAuthenticationTrigger(props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    public void verifyOperationInvalidProvider() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setSessionAttribute("mfaPolicy");
        httpRequest.setAttribute("mfaPolicy", "invalid");
        val trigger = new HttpRequestMultifactorAuthenticationTrigger(props, this.applicationContext);
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class)));
    }
}

