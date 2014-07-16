/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.ticket.enc;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * @author Misagh Moayyed
 */
public class SymmetricCipherEncoderTests {

    private static final String TICKET = "TGT-123456";

    private SymmetricCipherEncoder encoder;

    public SymmetricCipherEncoderTests() throws Exception {
        final SecretKeyFactoryBean fact = new SecretKeyFactoryBean();
        final SecretKey key = fact.createInstance();
        this.encoder = new SymmetricCipherEncoder(key);
    }

    @Test
    public void testStringEncryption() {
        final String encodedTicket = this.encoder.encode(TICKET.getBytes());
        assertNotNull(encodedTicket);
    }

    @Test
    public void testTgtDecryption() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TICKET, mock(Authentication.class), mock(ExpirationPolicy.class));
        final String encodedTicket = this.encoder.encode(t);
        final Object obj = this.encoder.decode(encodedTicket);
        assertNotNull(obj instanceof TicketGrantingTicket);
    }

    @Test
    public void testStDecryption() {
        final List<CredentialMetaData> creds = new ArrayList<CredentialMetaData>();
        final BasicCredentialMetaData metaData = new BasicCredentialMetaData(TestUtils.getCredentialsWithSameUsernameAndPassword());
        creds.add(metaData);

        final SimplePrincipal p = new SimplePrincipal("uid");

        final Map<String, Object> attrs = new HashMap<String, Object>();
        final Map<String, HandlerResult> successes = new HashMap<String, HandlerResult>();
        successes.put("handler", new HandlerResult(new SimpleTestUsernamePasswordAuthenticationHandler(),
                metaData, p));
        final Map<String, Class<? extends Exception>> failures = new HashMap<String, Class<? extends Exception>>();

        final Authentication auth = new ImmutableAuthentication(new Date(), creds,
                p, attrs, successes, failures);

        final TicketGrantingTicket tgt = new TicketGrantingTicketImpl(TICKET, auth, new NeverExpiresExpirationPolicy());
        final ServiceTicket t = tgt.grantServiceTicket(TICKET, TestUtils.getService(), new NeverExpiresExpirationPolicy(), false);
        final String encodedTicket = this.encoder.encode(t);
        final ServiceTicket obj = this.encoder.decode(encodedTicket, ServiceTicket.class);
        assertNotNull(obj);
        assertEquals(obj.getId(), TICKET);
    }
}
