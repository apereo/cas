package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.trigger.GlobalMultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GlobalMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class GlobalMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    public void verifyOperationByProvider() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGlobalProviderId(TestMultifactorAuthenticationProvider.ID);
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    public void verifyOperationByUnresolvedProvider() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGlobalProviderId("does-not-exist");
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext);
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class)));
    }

    @Test
    public void verifyOperationByUndefinedProvider() {
        val props = new CasConfigurationProperties();
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertFalse(result.isPresent());
    }
}
