package org.apereo.cas.mfa.simple;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleMultifactorAuthenticationUniqueTicketIdGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class CasSimpleMultifactorAuthenticationUniqueTicketIdGeneratorTests {
    @Test
    public void verifyOperation() {
        val gen = new CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator(6);
        assertNotNull(gen.getNewTicketId("CAS"));
    }
}
