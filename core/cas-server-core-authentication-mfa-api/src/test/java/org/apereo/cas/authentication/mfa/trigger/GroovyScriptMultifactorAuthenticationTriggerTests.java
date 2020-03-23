package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyScriptMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Groovy")
public class GroovyScriptMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    public void verifyOperationByProvider() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGroovyScript(new ClassPathResource("GroovyMfaTrigger.groovy"));
        val trigger = new GroovyScriptMultifactorAuthenticationTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    public void verifyScriptDoesNotExist() {
        val props = new CasConfigurationProperties();
        var trigger = new GroovyScriptMultifactorAuthenticationTrigger(props, applicationContext);
        var result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertFalse(result.isPresent());
        
        props.getAuthn().getMfa().setGroovyScript(new ClassPathResource("DoesNotExist.groovy"));
        trigger = new GroovyScriptMultifactorAuthenticationTrigger(props, applicationContext);
        result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertFalse(result.isPresent());
    }

    @Test
    public void verifyBadInputParameters() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGroovyScript(new ClassPathResource("GroovyMfaTrigger.groovy"));

        var trigger = new GroovyScriptMultifactorAuthenticationTrigger(props, applicationContext);
        var result = trigger.isActivated(null, registeredService, this.httpRequest, mock(Service.class));
        assertFalse(result.isPresent());

        result = trigger.isActivated(authentication, null, this.httpRequest, mock(Service.class));
        assertFalse(result.isPresent());

        result = trigger.isActivated(authentication, registeredService, this.httpRequest, null);
        assertFalse(result.isPresent());

        trigger.destroy();
    }
}
