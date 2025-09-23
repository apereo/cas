package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
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
 * This is {@link AuthenticationAttributeMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFATrigger")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthenticationAttributeMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    @Order(1)
    void verifyOperationByProvider() {
        val props = new CasConfigurationProperties();
        val mfa = props.getAuthn().getMfa().getTriggers().getAuthentication();
        mfa.setGlobalAuthenticationAttributeNameTriggers("category");
        mfa.setGlobalAuthenticationAttributeValueRegex(".+object.*");
        val trigger = new AuthenticationAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()),
            applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    @Order(2)
    void verifyMultipleProvider() {
        val otherProvider = new TestMultifactorAuthenticationProvider();
        otherProvider.setId("mfa-other");
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, otherProvider);

        val props = new CasConfigurationProperties();
        val mfa = props.getAuthn().getMfa().getTriggers().getAuthentication();
        mfa.setGlobalAuthenticationAttributeNameTriggers("mfa-mode");
        mfa.setGlobalAuthenticationAttributeValueRegex(otherProvider.getId());
        val trigger = new AuthenticationAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()),
            applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    @Order(3)
    void verifyNoMatch() {

        val props = new CasConfigurationProperties();
        val mfa = props.getAuthn().getMfa().getTriggers().getAuthentication();
        mfa.setGlobalAuthenticationAttributeNameTriggers("whatever");
        mfa.setGlobalAuthenticationAttributeValueRegex("whatever");
        val trigger = new AuthenticationAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()),
            applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isEmpty());
    }
}
