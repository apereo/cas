package org.apereo.cas.authentication.exceptions;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UniquePrincipalRequiredExceptionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
class UniquePrincipalRequiredExceptionTests {

    @Test
    void verifyOperation() {
        val input = new UniquePrincipalRequiredException();
        assertNotNull(input.getCode());

    }

}
