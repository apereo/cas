package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyScriptMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Groovy")
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GroovyScriptMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    @Order(1)
    public void verifyOperationByProvider() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGroovyScript(new ClassPathResource("GroovyMfaTrigger.groovy"));
        val trigger = new GroovyScriptMultifactorAuthenticationTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    @Order(2)
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
    @Order(3)
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

    @Test
    @Order(4)
    public void verifyFailProvider() throws Exception {
        val file = File.createTempFile("example", ".txt");
        FileUtils.writeStringToFile(file, "script", StandardCharsets.UTF_8);

        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGroovyScript(new FileSystemResource(file));
        val trigger = new GroovyScriptMultifactorAuthenticationTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isEmpty());
    }
    
    @Test
    @Order(0)
    @Tag("DisableProviderRegistration")
    public void verifyNoProvider() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGroovyScript(new ClassPathResource("GroovyMfaTrigger.groovy"));
        val trigger = new GroovyScriptMultifactorAuthenticationTrigger(props, applicationContext);
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class)));
    }

}
