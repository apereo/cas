package org.apereo.cas.authentication.support.password;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

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
class PasswordEncoderUtilsTests {
    private static final String RAW_PASSWORD = UUID.randomUUID().toString();

    private StaticApplicationContext applicationContext;

    private static void verifyEncodeAndMatch(final PasswordEncoder encoder) {
        assertNotNull(encoder);
        val result = encoder.encode(RAW_PASSWORD);
        assertTrue(encoder.matches(RAW_PASSWORD, result));
    }

    @BeforeEach
    void setup() {
        applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
    }

    @Test
    void verifyNoType() {
        val properties = new PasswordEncoderProperties();
        properties.setType(null);
        var encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        assertNotNull(encoder);
        properties.setType(StringUtils.EMPTY);
        encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        verifyEncodeAndMatch(encoder);
    }

    @Test
    void verifyArgon2() {
        val properties = new PasswordEncoderProperties();
        properties.setType(PasswordEncoderProperties.PasswordEncoderTypes.ARGON2.name());
        val encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        verifyEncodeAndMatch(encoder);
    }

    @Test
    void verifyBcryptType() {
        val properties = new PasswordEncoderProperties();
        properties.setSecret(null);
        properties.setType(PasswordEncoderProperties.PasswordEncoderTypes.BCRYPT.name());
        val encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        verifyEncodeAndMatch(encoder);
    }

    @Test
    void verifyPbkdf2() {
        val properties = new PasswordEncoderProperties();
        properties.setSecret(UUID.randomUUID().toString());
        properties.setStrength(16);
        properties.setType(PasswordEncoderProperties.PasswordEncoderTypes.PBKDF2.name());
        val encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        verifyEncodeAndMatch(encoder);
    }

    @Test
    void verifyGroovyType() {
        val properties = new PasswordEncoderProperties();
        properties.setType("classpath:/GroovyPasswordEncoder.groovy");
        val encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        verifyEncodeAndMatch(encoder);
    }

    @Test
    void verifyClassType() {
        val properties = new PasswordEncoderProperties();
        properties.setType("org.example.cas.SamplePasswordEncoder");
        val encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
        verifyEncodeAndMatch(encoder);
    }

    @Test
    void verifyAvailableTypes() {
        val secret = UUID.randomUUID().toString();
        Arrays.stream(PasswordEncoderProperties.PasswordEncoderTypes.values()).forEach(type -> {
            val properties = new PasswordEncoderProperties();
            properties.setSecret(secret);
            val algorithm = switch (type) {
                case PBKDF2 -> "PBKDF2WithHmacSHA512";
                default -> "SHA-256";
            };
            properties.setEncodingAlgorithm(algorithm);
            properties.setStrength(16);
            properties.setType(type.name());
            val encoder = PasswordEncoderUtils.newPasswordEncoder(properties, applicationContext);
            verifyEncodeAndMatch(encoder);
        });
    }
}
