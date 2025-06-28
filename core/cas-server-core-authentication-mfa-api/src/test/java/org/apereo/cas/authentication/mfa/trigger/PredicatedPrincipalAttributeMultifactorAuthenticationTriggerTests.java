package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PredicatedPrincipalAttributeMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("MFATrigger")
class PredicatedPrincipalAttributeMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    void verifyNoProviders() throws Throwable {
        val appContext = new StaticApplicationContext();
        appContext.refresh();
        
        val props = new CasConfigurationProperties();
        val file = Files.createTempFile("example", ".txt").toFile();
        FileUtils.writeStringToFile(file, "script", StandardCharsets.UTF_8);
        props.getAuthn().getMfa().getTriggers().getPrincipal().getGlobalPrincipalAttributePredicate().setLocation(new FileSystemResource(file));
        val trigger = new PredicatedPrincipalAttributeMultifactorAuthenticationTrigger(props, appContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isEmpty());
    }

    @Test
    void verifyOperationByHeader() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().getTriggers().getPrincipal().getGlobalPrincipalAttributePredicate().setLocation(new ClassPathResource("GroovyPredicate.groovy"));
        val trigger = new PredicatedPrincipalAttributeMultifactorAuthenticationTrigger(props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
    }


    @Test
    void verifyNoPredicate() throws Throwable {
        val props = new CasConfigurationProperties();
        val file = Files.createTempFile("predicate", ".txt").toFile();
        FileUtils.writeStringToFile(file, "script", StandardCharsets.UTF_8);
        props.getAuthn().getMfa().getTriggers().getPrincipal().getGlobalPrincipalAttributePredicate().setLocation(new FileSystemResource(file));
        val trigger = new PredicatedPrincipalAttributeMultifactorAuthenticationTrigger(props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isEmpty());
    }
}
