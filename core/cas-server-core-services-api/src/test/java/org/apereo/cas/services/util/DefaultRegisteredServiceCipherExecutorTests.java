package org.apereo.cas.services.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultRegisteredServiceCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class DefaultRegisteredServiceCipherExecutorTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void verifyCipherUnableToEncodeForStringIsTooLong() {
        final AbstractRegisteredService svc = getService("classpath:keys/RSA1024Public.key");

        final String ticketId = RandomStringUtils.randomAlphanumeric(120);
        final RegisteredServiceCipherExecutor e = new DefaultRegisteredServiceCipherExecutor();
        assertNull(e.encode(ticketId, svc));
    }

    @Test
    public void verifyCipherAbleToEncode() {
        final AbstractRegisteredService svc = getService("classpath:keys/RSA4096Public.key");
        final String ticketId = RandomStringUtils.randomAlphanumeric(120);
        final RegisteredServiceCipherExecutor e = new DefaultRegisteredServiceCipherExecutor();
        assertNotNull(e.encode(ticketId, svc));
    }

    private AbstractRegisteredService getService(final String keyLocation) {
        final AbstractRegisteredService svc = new RegexRegisteredService();
        svc.setServiceId("Testing");
        svc.setPublicKey(new RegisteredServicePublicKeyImpl(keyLocation, "RSA"));
        return svc;
    }
    
   
}
