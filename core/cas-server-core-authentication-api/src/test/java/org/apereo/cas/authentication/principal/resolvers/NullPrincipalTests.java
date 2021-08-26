package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.principal.NullPrincipal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link NullPrincipalTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
public class NullPrincipalTests {

    @Test
    public void verifyOperation() {
        assertNotNull(NullPrincipal.getInstance().getId());
    }

}
