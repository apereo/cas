package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.trigger.PredicatedPrincipalAttributeMultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PredicatedPrincipalAttributeMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("MFA")
public class PredicatedPrincipalAttributeMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    public void verifyOperationByHeader() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGlobalPrincipalAttributePredicate(new ClassPathResource("GroovyPredicate.groovy"));
        val trigger = new PredicatedPrincipalAttributeMultifactorAuthenticationTrigger(props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }
}
