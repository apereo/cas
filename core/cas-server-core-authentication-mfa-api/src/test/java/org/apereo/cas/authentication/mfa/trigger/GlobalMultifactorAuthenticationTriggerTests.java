package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GlobalMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFATrigger")
class GlobalMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    void verifyNoProvider() {
        val appContext = new StaticApplicationContext();
        appContext.refresh();
        
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().getTriggers().getGlobal().setGlobalProviderId(TestMultifactorAuthenticationProvider.ID);
        val trigger = getTrigger(props, appContext);
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class)));
    }

    @Test
    void verifyOperationByProvider() throws Throwable {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().getTriggers().getGlobal().setGlobalProviderId(TestMultifactorAuthenticationProvider.ID);
        val trigger = getTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    void verifyOperationByManyProviders() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().getTriggers().getGlobal().setGlobalProviderId(TestMultifactorAuthenticationProvider.ID + ",mfa-invalid");
        val trigger = getTrigger(props, applicationContext);
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class)));
    }

    @Test
    void verifyOperationByValidProviders() throws Throwable {
        val props = new CasConfigurationProperties();

        val otherProvider = new TestMultifactorAuthenticationProvider();
        otherProvider.setId("mfa-other");
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, otherProvider);

        props.getAuthn().getMfa().getTriggers().getGlobal().setGlobalProviderId(TestMultifactorAuthenticationProvider.ID + ',' + otherProvider.getId());
        val trigger = getTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
        assertEquals(TestMultifactorAuthenticationProvider.ID, result.get().getId());
    }

    @Test
    void verifyOperationByUnresolvedProvider() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().getTriggers().getGlobal().setGlobalProviderId("does-not-exist");
        val trigger = getTrigger(props, applicationContext);
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class)));
    }

    @Test
    void verifyOperationByUndefinedProvider() throws Throwable {
        val props = new CasConfigurationProperties();
        val trigger = getTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertFalse(result.isPresent());
    }

    private static MultifactorAuthenticationTrigger getTrigger(final CasConfigurationProperties props,
                                                               final ConfigurableApplicationContext applicationContext) {
        return new GlobalMultifactorAuthenticationTrigger(props, applicationContext,
            (providers, service, principal) -> providers.iterator().next());
    }
}
