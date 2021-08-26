package org.apereo.cas.rest.factory;

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
public class UsernamePasswordRestHttpRequestCredentialFactoryTests {
    @Test
    public void verifyOperation() {
        val factory = new UsernamePasswordRestHttpRequestCredentialFactory();
        assertEquals(Integer.MIN_VALUE, factory.getOrder());
    }
}
