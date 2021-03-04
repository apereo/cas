package org.apereo.cas.authentication;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAuthenticationResultBuilderFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Authentication")
public class DefaultAuthenticationResultBuilderFactoryTests {
    @Test
    public void verifyOperation() {
        assertNotNull(new DefaultAuthenticationResultBuilderFactory().newBuilder());
    }
}
