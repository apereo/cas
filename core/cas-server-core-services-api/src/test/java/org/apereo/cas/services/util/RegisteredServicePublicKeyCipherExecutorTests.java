package org.apereo.cas.services.util;

import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;

import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * This is {@link RegisteredServicePublicKeyCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RegisteredServicePublicKeyCipherExecutorTests {

    @Test
    public void verifyCipherUnableToEncodeForStringIsTooLong() {
        val svc = getService("classpath:keys/RSA1024Public.key");

        val ticketId = RandomStringUtils.randomAlphanumeric(120);
        val e = new RegisteredServicePublicKeyCipherExecutor();
        assertNull(e.encode(ticketId, Optional.of(svc)));
    }

    @Test
    public void verifyCipherAbleToEncode() {
        val svc = getService("classpath:keys/RSA4096Public.key");
        val ticketId = RandomStringUtils.randomAlphanumeric(120);
        val e = new RegisteredServicePublicKeyCipherExecutor();
        assertNotNull(e.encode(ticketId, Optional.of(svc)));
    }

    private static AbstractRegisteredService getService(final String keyLocation) {
        val svc = new RegexRegisteredService();
        svc.setServiceId("Testing");
        svc.setPublicKey(new RegisteredServicePublicKeyImpl(keyLocation, "RSA"));
        return svc;
    }


}
