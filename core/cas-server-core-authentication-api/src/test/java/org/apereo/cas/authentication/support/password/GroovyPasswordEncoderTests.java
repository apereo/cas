package org.apereo.cas.authentication.support.password;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyPasswordEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Groovy")
public class GroovyPasswordEncoderTests {

    @Test
    public void verifyOperation() {
        val enc = new GroovyPasswordEncoder(new ClassPathResource("GroovyPasswordEncoder.groovy"));
        assertTrue(enc.matches("helloworld", "6adfb183a4a2c94a2f92dab5ade762a47889a5a1"));
    }
}
