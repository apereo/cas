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
@Tag("MFATrigger")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PredicatedPrincipalAttributeMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    @Order(0)
    @Tag("DisableProviderRegistration")
    void verifyNoProviders() throws Throwable {
        val props = new CasConfigurationProperties();
        val file = File.createTempFile("example", ".txt");
        FileUtils.writeStringToFile(file, "script", StandardCharsets.UTF_8);
        props.getAuthn().getMfa().getTriggers().getPrincipal().getGlobalPrincipalAttributePredicate().setLocation(new FileSystemResource(file));
        val trigger = new PredicatedPrincipalAttributeMultifactorAuthenticationTrigger(props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isEmpty());
    }

    @Test
    @Order(1)
    void verifyOperationByHeader() throws Throwable {
        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().getTriggers().getPrincipal().getGlobalPrincipalAttributePredicate().setLocation(new ClassPathResource("GroovyPredicate.groovy"));
        val trigger = new PredicatedPrincipalAttributeMultifactorAuthenticationTrigger(props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
    }


    @Test
    @Order(3)
    void verifyNoPredicate() throws Throwable {
        val props = new CasConfigurationProperties();
        val file = File.createTempFile("predicate", ".txt");
        FileUtils.writeStringToFile(file, "script", StandardCharsets.UTF_8);
        props.getAuthn().getMfa().getTriggers().getPrincipal().getGlobalPrincipalAttributePredicate().setLocation(new FileSystemResource(file));
        val trigger = new PredicatedPrincipalAttributeMultifactorAuthenticationTrigger(props, this.applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isEmpty());
    }
}
