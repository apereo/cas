package org.apereo.cas.mfa.simple;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleMultifactorAuthenticationUniqueTicketIdGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFA")
public class CasSimpleMultifactorAuthenticationUniqueTicketIdGeneratorTests {
    @Test
    public void verifyOperation() {
        val gen = new CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator(6);
        assertNotNull(gen.getNewTicketId("CAS"));
    }
}
