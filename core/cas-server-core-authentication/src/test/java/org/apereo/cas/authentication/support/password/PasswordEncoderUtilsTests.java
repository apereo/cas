package org.apereo.cas.authentication.support.password;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordEncoderUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */

@Tag("Utility")
public class PasswordEncoderUtilsTests {

    @Test
    public void verifyNoType() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val properties = new PasswordEncoderProperties();
        properties.setType(null);
        var encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        assertNotNull(encoder);
        properties.setType(StringUtils.EMPTY);
        encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        assertNotNull(encoder);
    }

    @Test
    public void verifyBcryptType() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val properties = new PasswordEncoderProperties();
        properties.setSecret(null);
        properties.setType(PasswordEncoderProperties.PasswordEncoderTypes.BCRYPT.name());
        val encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        assertNotNull(encoder);
    }

    @Test
    public void verifyPbkdf2() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val properties = new PasswordEncoderProperties();
        properties.setSecret(null);
        properties.setType(PasswordEncoderProperties.PasswordEncoderTypes.PBKDF2.name());
        val encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        assertNotNull(encoder);
    }
    
    @Test
    public void verifyGroovyType() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val properties = new PasswordEncoderProperties();
        properties.setType("sample-encoder.groovy");
        val encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        assertNotNull(encoder);
    }

    @Test
    public void verifyClassType() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val properties = new PasswordEncoderProperties();
        properties.setType("org.example.cas.SamplePasswordEncoder");
        val encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        assertNotNull(encoder);
    }

    @Test
    public void verifyAvailableTypes() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val secret = UUID.randomUUID().toString();
        Arrays.stream(PasswordEncoderProperties.PasswordEncoderTypes.values()).forEach(type -> {
            val properties = new PasswordEncoderProperties();
            properties.setSecret(secret);
            properties.setStrength(16);
            properties.setType(type.name());
            val encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
            assertNotNull(encoder);
        });
    }
}
