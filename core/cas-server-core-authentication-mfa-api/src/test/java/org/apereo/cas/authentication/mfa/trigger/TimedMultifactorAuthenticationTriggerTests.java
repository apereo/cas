package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.TimeBasedAuthenticationProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link TimedMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("MFATrigger")
class TimedMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    void verifyUndefined() {
        val props = new CasConfigurationProperties();
        var trigger = new TimedMultifactorAuthenticationTrigger(props, applicationContext);
        var result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertFalse(result.isPresent());

        trigger = new TimedMultifactorAuthenticationTrigger(props, applicationContext);
        result = trigger.isActivated(null, null, this.httpRequest, this.httpResponse, mock(Service.class));
        assertFalse(result.isPresent());
    }

    @Test
    void verifyProvider() {
        val props = new CasConfigurationProperties();
        val timeProps = new TimeBasedAuthenticationProperties();
        timeProps.setProviderId(TestMultifactorAuthenticationProvider.ID);
        timeProps.setOnOrAfterHour(0);
        timeProps.setOnOrBeforeHour(24);
        timeProps.setOnDays(List.of("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"));
        props.getAuthn().getAdaptive().getPolicy().getRequireTimedMultifactor().add(timeProps);

        var trigger = new TimedMultifactorAuthenticationTrigger(props, applicationContext);
        var result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());

        timeProps.setProviderId("bad-id");
        val trigger2 = new TimedMultifactorAuthenticationTrigger(props, applicationContext);
        assertThrows(AuthenticationException.class,
            () -> trigger2.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class)));
    }

    @Test
    void verifyNoProviders() {
        val props = new CasConfigurationProperties();

        val appContext = new StaticApplicationContext();
        appContext.refresh();

        val trigger = new TimedMultifactorAuthenticationTrigger(props, appContext);
        val timeProps = new TimeBasedAuthenticationProperties();
        timeProps.setProviderId(TestMultifactorAuthenticationProvider.ID);
        timeProps.setOnOrAfterHour(2);
        timeProps.setOnOrBeforeHour(2);
        timeProps.setOnDays(List.of("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"));

        props.getAuthn().getAdaptive().getPolicy().getRequireTimedMultifactor().add(timeProps);
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class)));
    }
}
