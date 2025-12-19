package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServicePublicKeyCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Cipher")
class RegisteredServicePublicKeyCipherExecutorTests {

    private static BaseRegisteredService getService(final String keyLocation) {
        val svc = new CasRegisteredService();
        svc.setServiceId("Testing");
        if (StringUtils.isNotBlank(keyLocation)) {
            svc.setPublicKey(new RegisteredServicePublicKeyImpl(keyLocation, "RSA"));
        }
        return svc;
    }

    @Test
    void verifyCipherUnableToEncodeForStringIsTooLong() {
        val svc = getService("classpath:keys/RSA1024Public.key");
        val ticketId = RandomUtils.randomAlphanumeric(120);
        val e = new RegisteredServicePublicKeyCipherExecutor();
        assertNull(e.encode(ticketId, Optional.of(svc)));
    }

    @Test
    void verifyCipherAbleToEncode() {
        val svc = getService("classpath:keys/RSA4096Public.key");
        val ticketId = RandomUtils.randomAlphanumeric(120);
        val e = new RegisteredServicePublicKeyCipherExecutor();
        assertNotNull(e.encode(ticketId, Optional.of(svc)));
        assertNull(e.decode(ticketId, Optional.of(svc)));
    }

    @Test
    void verifyCipherNoKey() {
        val svc = getService(StringUtils.EMPTY);
        val ticketId = RandomUtils.randomAlphanumeric(120);
        val e = new RegisteredServicePublicKeyCipherExecutor();
        assertNull(e.encode(ticketId, Optional.of(svc)));
    }
}
