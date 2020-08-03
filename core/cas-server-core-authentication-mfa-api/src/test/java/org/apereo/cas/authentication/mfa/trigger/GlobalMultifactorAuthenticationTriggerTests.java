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
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GlobalMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFA")
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GlobalMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    @Order(0)
    @Tag("DisableProviderRegistration")
    public void verifyNoProvider() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGlobalProviderId(TestMultifactorAuthenticationProvider.ID);
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext,
            (providers, service, principal) -> providers.iterator().next());
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class)));
    }

    @Test
    @Order(1)
    public void verifyOperationByProvider() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGlobalProviderId(TestMultifactorAuthenticationProvider.ID);
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext,
            (providers, service, principal) -> providers.iterator().next());
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    @Order(2)
    public void verifyOperationByManyProviders() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGlobalProviderId(TestMultifactorAuthenticationProvider.ID + ",mfa-invalid");
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext,
            (providers, service, principal) -> providers.iterator().next());
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class)));
    }

    @Test
    @Order(3)
    public void verifyOperationByValidProviders() {
        val props = new CasConfigurationProperties();

        val otherProvider = new TestMultifactorAuthenticationProvider();
        otherProvider.setId("mfa-other");
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, otherProvider);

        props.getAuthn().getMfa().setGlobalProviderId(TestMultifactorAuthenticationProvider.ID + ',' + otherProvider.getId());
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext,
            (providers, service, principal) -> providers.iterator().next());
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
        assertEquals(TestMultifactorAuthenticationProvider.ID, result.get().getId());
    }

    @Test
    @Order(4)
    public void verifyOperationByUnresolvedProvider() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGlobalProviderId("does-not-exist");
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext,
            (providers, service, principal) -> providers.iterator().next());
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class)));
    }

    @Test
    @Order(5)
    public void verifyOperationByUndefinedProvider() {
        val props = new CasConfigurationProperties();
        val trigger = new GlobalMultifactorAuthenticationTrigger(props, applicationContext,
            (providers, service, principal) -> providers.iterator().next());
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertFalse(result.isPresent());
    }
}
