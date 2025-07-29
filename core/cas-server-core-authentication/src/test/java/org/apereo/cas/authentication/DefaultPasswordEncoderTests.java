package org.apereo.cas.authentication;

import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.util.crypto.DefaultPasswordEncoder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultPasswordEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("PasswordOps")
class DefaultPasswordEncoderTests {

    @Test
    void verifyPasswordEncoderByCustomClassName() {
        val properties = new PasswordEncoderProperties();
        properties.setType(StandardPasswordEncoder.class.getName());
        properties.setSecret("SECRET");
        val e = PasswordEncoderUtils.newPasswordEncoder(properties, mock(ApplicationContext.class));
        assertNotNull(e);
    }

    @Test
    void verifyPasswordEncoderByMD5() {
        val properties = new PasswordEncoderProperties();
        properties.setType(PasswordEncoderProperties.PasswordEncoderTypes.DEFAULT.name());
        properties.setEncodingAlgorithm("MD5");
        properties.setCharacterEncoding("UTF-8");
        val e = PasswordEncoderUtils.newPasswordEncoder(properties, mock(ApplicationContext.class));
        assertTrue(e.matches("asd123", "bfd59291e825b5f2bbf1eb76569f8fe7"));
    }

    @Test
    void verifyPasswordEncoderBySHA1() {
        val properties = new PasswordEncoderProperties();
        properties.setType(PasswordEncoderProperties.PasswordEncoderTypes.DEFAULT.name());
        properties.setEncodingAlgorithm("SHA-1");
        properties.setCharacterEncoding("UTF-8");
        val e = PasswordEncoderUtils.newPasswordEncoder(properties, mock(ApplicationContext.class));
        assertTrue(e.matches("asd123", "2891baceeef1652ee698294da0e71ba78a2a4064"));
    }

    @Test
    void verifyPasswordEncoderBySHA256() {
        val p = new PasswordEncoderProperties();
        p.setType(PasswordEncoderProperties.PasswordEncoderTypes.DEFAULT.name());
        p.setEncodingAlgorithm("SHA-256");
        p.setCharacterEncoding("UTF-8");
        val e = PasswordEncoderUtils.newPasswordEncoder(p, mock(ApplicationContext.class));
        assertTrue(e.matches("asd123", "54d5cb2d332dbdb4850293caae4559ce88b65163f1ea5d4e4b3ac49d772ded14"));
    }

    @Test
    void verifyBadInput() {
        val encoder = new DefaultPasswordEncoder(null, null);
        assertNull(encoder.encode(null));
        assertNull(encoder.encode("password"));
    }

    @Test
    void verifyBadAlg() {
        val encoder = new DefaultPasswordEncoder("BadAlgorithm", null);
        assertNull(encoder.encode("password"));
    }
}
