package org.apereo.cas.authentication.support.password;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordEncoderUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@Tag("Simple")
public class PasswordEncoderUtilsTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyNoType() {
        val properties = new PasswordEncoderProperties();
        properties.setType(null);
        var encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        assertNotNull(encoder);
        properties.setType(StringUtils.EMPTY);
        encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        assertNotNull(encoder);
    }

    @Test
    public void verifyGroovyType() {
        val properties = new PasswordEncoderProperties();
        properties.setType("sample-encoder.groovy");
        val encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        assertNotNull(encoder);
    }

    @Test
    public void verifyClassType() {
        val properties = new PasswordEncoderProperties();
        properties.setType("org.example.cas.SamplePasswordEncoder");
        val encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        assertNotNull(encoder);
    }

    @Test
    public void verifyAvailableTypes() {
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
