package org.apereo.cas.services;

import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServicePublicKeyCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("RegisteredService")
public class RegisteredServicePublicKeyCipherExecutorTests {

    @Test
    public void verifyCipherUnableToEncodeForStringIsTooLong() {
        val svc = getService("classpath:keys/RSA1024Public.key");
        val ticketId = RandomUtils.randomAlphanumeric(120);
        val e = new RegisteredServicePublicKeyCipherExecutor();
        assertNull(e.encode(ticketId, Optional.of(svc)));
    }

    @Test
    public void verifyCipherAbleToEncode() {
        val svc = getService("classpath:keys/RSA4096Public.key");
        val ticketId = RandomUtils.randomAlphanumeric(120);
        val e = new RegisteredServicePublicKeyCipherExecutor();
        assertNotNull(e.encode(ticketId, Optional.of(svc)));
        assertNull(e.decode(ticketId, Optional.of(svc)));
    }

    @Test
    public void verifyCipherNoKey() {
        val svc = getService(StringUtils.EMPTY);
        val ticketId = RandomUtils.randomAlphanumeric(120);
        val e = new RegisteredServicePublicKeyCipherExecutor();
        assertNull(e.encode(ticketId, Optional.of(svc)));
    }

    private static AbstractRegisteredService getService(final String keyLocation) {
        val svc = new RegexRegisteredService();
        svc.setServiceId("Testing");
        if (StringUtils.isNotBlank(keyLocation)) {
            svc.setPublicKey(new RegisteredServicePublicKeyImpl(keyLocation, "RSA"));
        }
        return svc;
    }
}
