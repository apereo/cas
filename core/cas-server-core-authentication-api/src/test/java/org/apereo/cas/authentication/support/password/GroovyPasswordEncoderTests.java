package org.apereo.cas.authentication.support.password;

import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyPasswordEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class GroovyPasswordEncoderTests {

    @Test
    public void verifyOperation() {
        val enc = new GroovyPasswordEncoder(new ClassPathResource("GroovyPasswordEncoder.groovy"));
        val encoded = enc.encode("helloworld");
        assertTrue(enc.matches("helloworld", encoded));
    }
}
