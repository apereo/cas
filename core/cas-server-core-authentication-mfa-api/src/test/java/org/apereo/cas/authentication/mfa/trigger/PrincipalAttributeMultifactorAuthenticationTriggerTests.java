package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.trigger.PrincipalAttributeMultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PrincipalAttributeMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class PrincipalAttributeMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    public void verifyOperationByProvider() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGlobalPrincipalAttributeNameTriggers("email");
        props.getAuthn().getMfa().setGlobalPrincipalAttributeValueRegex(".+@example.*");
        val trigger = new PrincipalAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver((providers, service, principal) -> providers.iterator().next()),
            applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }
}
