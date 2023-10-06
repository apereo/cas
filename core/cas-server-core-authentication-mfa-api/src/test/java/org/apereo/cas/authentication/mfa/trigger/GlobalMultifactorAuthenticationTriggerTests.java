package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GlobalMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFATrigger")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GlobalMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    @Order(0)
    @Tag("DisableProviderRegistration")
    void verifyNoProvider() throws Throwable {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().getTriggers().getGlobal().setGlobalProviderId(TestMultifactorAuthenticationProvider.ID);
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext,
            (providers, service, principal) -> providers.iterator().next());
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class)));
    }

    @Test
    @Order(1)
    void verifyOperationByProvider() throws Throwable {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().getTriggers().getGlobal().setGlobalProviderId(TestMultifactorAuthenticationProvider.ID);
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext,
            (providers, service, principal) -> providers.iterator().next());
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    @Order(2)
    void verifyOperationByManyProviders() throws Throwable {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().getTriggers().getGlobal().setGlobalProviderId(TestMultifactorAuthenticationProvider.ID + ",mfa-invalid");
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext,
            (providers, service, principal) -> providers.iterator().next());
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class)));
    }

    @Test
    @Order(3)
    void verifyOperationByValidProviders() throws Throwable {
        val props = new CasConfigurationProperties();

        val otherProvider = new TestMultifactorAuthenticationProvider();
        otherProvider.setId("mfa-other");
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, otherProvider);

        props.getAuthn().getMfa().getTriggers().getGlobal().setGlobalProviderId(TestMultifactorAuthenticationProvider.ID + ',' + otherProvider.getId());
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext,
            (providers, service, principal) -> providers.iterator().next());
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
        assertEquals(TestMultifactorAuthenticationProvider.ID, result.get().getId());
    }

    @Test
    @Order(4)
    void verifyOperationByUnresolvedProvider() throws Throwable {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().getTriggers().getGlobal().setGlobalProviderId("does-not-exist");
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext,
            (providers, service, principal) -> providers.iterator().next());
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class)));
    }

    @Test
    @Order(5)
    void verifyOperationByUndefinedProvider() throws Throwable {
        val props = new CasConfigurationProperties();
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext,
            (providers, service, principal) -> providers.iterator().next());
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertFalse(result.isPresent());
    }
}
