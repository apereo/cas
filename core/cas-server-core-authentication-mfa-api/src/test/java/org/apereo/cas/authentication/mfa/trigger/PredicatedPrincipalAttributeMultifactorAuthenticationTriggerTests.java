package org.apereo.cas.authentication.mfa.trigger;

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
 * This is {@link PredicatedPrincipalAttributeMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("MFA")
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PredicatedPrincipalAttributeMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    @Order(0)
    @Tag("DisableProviderRegistration")
    public void verifyNoProviders() throws Exception {
        val props = new CasConfigurationProperties();
        val file = File.createTempFile("example", ".txt");
        FileUtils.writeStringToFile(file, "script", StandardCharsets.UTF_8);
        props.getAuthn().getMfa().setGlobalPrincipalAttributePredicate(new FileSystemResource(file));
        val trigger = new PredicatedPrincipalAttributeMultifactorAuthenticationTrigger(props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isEmpty());
    }

    @Test
    @Order(1)
    public void verifyOperationByHeader() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().setGlobalPrincipalAttributePredicate(new ClassPathResource("GroovyPredicate.groovy"));
        val trigger = new PredicatedPrincipalAttributeMultifactorAuthenticationTrigger(props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }


    @Test
    @Order(3)
    public void verifyNoPredicate() throws Exception {
        val props = new CasConfigurationProperties();
        val file = File.createTempFile("predicate", ".txt");
        FileUtils.writeStringToFile(file, "script", StandardCharsets.UTF_8);
        props.getAuthn().getMfa().setGlobalPrincipalAttributePredicate(new FileSystemResource(file));
        val trigger = new PredicatedPrincipalAttributeMultifactorAuthenticationTrigger(props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isEmpty());
    }
}
