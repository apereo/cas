package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PrincipalAttributeMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFA")
public class PrincipalAttributeMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    public void verifyOperationByProvider() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGlobalPrincipalAttributeNameTriggers("email");
        props.getAuthn().getMfa().setGlobalPrincipalAttributeValueRegex(".+@example.*");
        val resolver = new DefaultMultifactorAuthenticationProviderResolver();

        val trigger = new PrincipalAttributeMultifactorAuthenticationTrigger(props, resolver, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }
}
