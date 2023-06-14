package org.apereo.cas.services.util;

import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceNoOpCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Cipher")
class RegisteredServiceNoOpCipherExecutorTests {

    private static BaseRegisteredService getService(final String keyLocation) {
        val svc = new CasRegisteredService();
        svc.setServiceId("Testing");
        svc.setPublicKey(new RegisteredServicePublicKeyImpl(keyLocation, "RSA"));
        return svc;
    }

    @Test
    void verifyCipherUnableToEncodeForStringIsTooLong() {
        val svc = getService("classpath:keys/RSA1024Public.key");
        val ticketId = RandomUtils.randomAlphanumeric(120);
        val cipher = RegisteredServiceCipherExecutor.noOp();
        assertEquals(ticketId, cipher.encode(ticketId, Optional.of(svc)));
        assertEquals(ticketId, cipher.encode(ticketId));
        assertFalse(cipher.supports(svc));
        assertFalse(cipher.isEnabled());
    }
}
