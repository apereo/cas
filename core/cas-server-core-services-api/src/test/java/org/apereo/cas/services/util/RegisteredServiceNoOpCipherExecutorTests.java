package org.apereo.cas.services.util;

import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
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
@Tag("Simple")
public class RegisteredServiceNoOpCipherExecutorTests {

    @Test
    public void verifyCipherUnableToEncodeForStringIsTooLong() {
        val svc = getService("classpath:keys/RSA1024Public.key");
        val ticketId = RandomUtils.randomAlphanumeric(120);
        val cipher = RegisteredServiceCipherExecutor.noOp();
        assertEquals(ticketId, cipher.encode(ticketId, Optional.of(svc)));
        assertEquals(ticketId, cipher.encode(ticketId));
        assertFalse(cipher.supports(svc));
        assertFalse(cipher.isEnabled());
    }

    private static AbstractRegisteredService getService(final String keyLocation) {
        val svc = new RegexRegisteredService();
        svc.setServiceId("Testing");
        svc.setPublicKey(new RegisteredServicePublicKeyImpl(keyLocation, "RSA"));
        return svc;
    }
}
