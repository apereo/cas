package org.apereo.cas.services.util;

import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
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
public class DefaultRegisteredServiceCipherExecutorTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void verifyCipherUnableToEncodeForStringIsTooLong() {
        final AbstractRegisteredService svc = getService("classpath:keys/RSA1024Public.key");

        final String ticketId = getStringToEncode();
        final RegisteredServiceCipherExecutor e = new DefaultRegisteredServiceCipherExecutor();
        assertNull(e.encode(ticketId, svc));
    }

    @Test
    public void verifyCipherAbleToEncode() {
        final AbstractRegisteredService svc = getService("classpath:keys/RSA4096Public.key");
        final String ticketId = getStringToEncode();
        final RegisteredServiceCipherExecutor e = new DefaultRegisteredServiceCipherExecutor();
        assertNotNull(e.encode(ticketId, svc));
    }

    private AbstractRegisteredService getService(final String keyLocation) {
        final AbstractRegisteredService svc = RegisteredServiceTestUtils.getRegisteredService("test");
        svc.setPublicKey(new RegisteredServicePublicKeyImpl(keyLocation, "RSA"));
        return svc;
    }
    
    private String getStringToEncode() {
        final UniqueTicketIdGenerator gen = new DefaultUniqueTicketIdGenerator(100, "testing-gce-52ac2b2f-3d76-42e0-8d58-7ec8ff76a287");
        return gen.getNewTicketId("TEST");
    }
}
