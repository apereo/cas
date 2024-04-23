package org.apereo.cas.mfa.simple;

import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator;

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
@Tag("MFAProvider")
class CasSimpleMultifactorAuthenticationUniqueTicketIdGeneratorTests {
    @Test
    void verifyOperation() throws Throwable {
        val gen = new CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator(6);
        assertNotNull(gen.getNewTicketId("CAS"));
    }
}
