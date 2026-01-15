package org.apereo.cas.rest.factory;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UsernamePasswordRestHttpRequestCredentialFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Authentication")
class UsernamePasswordRestHttpRequestCredentialFactoryTests {
    @Test
    void verifyOperation() {
        val factory = new UsernamePasswordRestHttpRequestCredentialFactory();
        assertEquals(Integer.MIN_VALUE, factory.getOrder());
    }
}
