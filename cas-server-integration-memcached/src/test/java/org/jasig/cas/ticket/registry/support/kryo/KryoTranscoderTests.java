/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.ticket.registry.support.kryo;

import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import net.spy.memcached.CachedData;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.AcceptUsersAuthenticationHandler;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.DefaultAuthenticationBuilder;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.RememberMeCredential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Test;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link KryoTranscoder} class.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@SuppressWarnings("rawtypes")
public class KryoTranscoderTests {

    private static final String ST_ID = "ST-1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890ABCDEFGHIJK";
    private static final String TGT_ID = "TGT-1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890ABCDEFGHIJK-cas1";

    private static final String USERNAME = "handymanbob";
    private static final String PASSWORD = "foo";
    private static final String NICKNAME_KEY = "nickname";
    private static final String NICKNAME_VALUE = "bob";

    private final KryoTranscoder transcoder;

    private final Map<String, Object> principalAttributes;

    public KryoTranscoderTests() {
        transcoder = new KryoTranscoder();
        final Map<Class<?>, Serializer> serializerMap = new HashMap<Class<?>, Serializer>();
        serializerMap.put(
                MockServiceTicket.class,
                new FieldSerializer(transcoder.getKryo(), MockServiceTicket.class));
        serializerMap.put(
                MockTicketGrantingTicket.class,
                new FieldSerializer(transcoder.getKryo(), MockTicketGrantingTicket.class));
        transcoder.setSerializerMap(serializerMap);
        transcoder.initialize();

        this.principalAttributes = new HashMap<>();
        this.principalAttributes.put(NICKNAME_KEY, NICKNAME_VALUE);
    }

    @Test
    public void verifyEncodeDecodeTGTImpl() throws Exception {
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final AuthenticationBuilder bldr = new DefaultAuthenticationBuilder(
                new DefaultPrincipalFactory()
                        .createPrincipal("user", Collections.unmodifiableMap(this.principalAttributes)));
        bldr.setAttributes(Collections.unmodifiableMap(this.principalAttributes));
        bldr.setAuthenticationDate(new Date());
        bldr.addCredential(new BasicCredentialMetaData(userPassCredential));
        bldr.addFailure("error", AccountNotFoundException.class);
        bldr.addSuccess("authn", new DefaultHandlerResult(
                new AcceptUsersAuthenticationHandler(),
                new BasicCredentialMetaData(userPassCredential)));

        final TicketGrantingTicket parent =
                new TicketGrantingTicketImpl(TGT_ID, TestUtils.getService(), null, bldr.build(),
                        new NeverExpiresExpirationPolicy());

        final TicketGrantingTicket expectedTGT =
                new TicketGrantingTicketImpl(TGT_ID, TestUtils.getService(),
                        null, bldr.build(),
                        new NeverExpiresExpirationPolicy());

        final ServiceTicket ticket = expectedTGT.grantServiceTicket(ST_ID,
                TestUtils.getService(),
                new NeverExpiresExpirationPolicy(), false);
        CachedData result = transcoder.encode(expectedTGT);
        final TicketGrantingTicket resultTicket = (TicketGrantingTicket) transcoder.decode(result);

        assertEquals(expectedTGT, resultTicket);
        result = transcoder.encode(ticket);
        final ServiceTicket resultStTicket = (ServiceTicket) transcoder.decode(result);
        assertEquals(ticket, resultStTicket);

    }

    @Test
    public void verifyEncodeDecode() throws Exception {
        final ServiceTicket expectedST =
                new MockServiceTicket(ST_ID);
        assertEquals(expectedST, transcoder.decode(transcoder.encode(expectedST)));

        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final TicketGrantingTicket expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, this.principalAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));

        internalProxyTest("http://localhost");
        internalProxyTest("https://localhost:8080/path/file.html?p1=v1&p2=v2#fragment");
    }

    private void internalProxyTest(final String proxyUrl) throws MalformedURLException {
        final Credential proxyCredential = new HttpBasedServiceCredential(new URL(proxyUrl), TestUtils.getRegisteredService("https://.+"));
        final TicketGrantingTicket expectedTGT = new MockTicketGrantingTicket(TGT_ID, proxyCredential, this.principalAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));        
    }

    @Test
    public void verifyEncodeDecodeTGTWithUnmodifiableMap() throws Exception {
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final TicketGrantingTicket expectedTGT =
                new MockTicketGrantingTicket(TGT_ID, userPassCredential, Collections.unmodifiableMap(this.principalAttributes));
        expectedTGT.grantServiceTicket(ST_ID, null, null, false);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeTGTWithUnmodifiableList() throws Exception {
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final List<String> values = new ArrayList<>();
        values.add(NICKNAME_VALUE);
        final Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put(NICKNAME_KEY, Collections.unmodifiableList(values));
        final TicketGrantingTicket expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeTGTWithLinkedHashMap() throws Exception {
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final TicketGrantingTicket expectedTGT =
                new MockTicketGrantingTicket(TGT_ID, userPassCredential, new LinkedHashMap<String, Object>(this.principalAttributes));
        expectedTGT.grantServiceTicket(ST_ID, null, null, false);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeTGTWithListOrderedMap() throws Exception {
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        @SuppressWarnings("unchecked")
        final TicketGrantingTicket expectedTGT =
                new MockTicketGrantingTicket(TGT_ID, userPassCredential, new LinkedHashMap<>(this.principalAttributes));
        expectedTGT.grantServiceTicket(ST_ID, null, null, false);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeTGTWithUnmodifiableSet() throws Exception {
        final Map<String, Object> newAttributes = new HashMap<>();
        final Set<String> values = new HashSet<>();
        values.add(NICKNAME_VALUE);
        newAttributes.put(NICKNAME_KEY, Collections.unmodifiableSet(values));
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final TicketGrantingTicket expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeTGTWithSingleton() throws Exception {
        final Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put(NICKNAME_KEY, Collections.singleton(NICKNAME_VALUE));
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final TicketGrantingTicket expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeTGTWithSingletonMap() throws Exception {
        final Map<String, Object> newAttributes = Collections.singletonMap(NICKNAME_KEY, (Object) NICKNAME_VALUE);
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final TicketGrantingTicket expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeRegisteredService() throws Exception {
        final RegisteredService service = TestUtils.getRegisteredService("helloworld");
        assertEquals(service, transcoder.decode(transcoder.encode(service)));
    }

    static class MockServiceTicket implements ServiceTicket {

        private static final long serialVersionUID = -206395373480723831L;
        private String id;

        MockServiceTicket() { /* for serialization */ }

        MockServiceTicket(final String id) {
            this.id = id;
        }

        public Service getService() {
            return null;
        }

        public boolean isFromNewLogin() {
            return false;
        }

        public boolean isValidFor(final Service service) {
            return false;
        }

        public TicketGrantingTicket grantTicketGrantingTicket(final String id, final Authentication authentication,
                final ExpirationPolicy expirationPolicy) {
            return null;
        }

        public String getId() {
            return id;
        }

        public boolean isExpired() {
            return false;
        }

        public TicketGrantingTicket getGrantingTicket() {
            return null;
        }

        public long getCreationTime() {
            return 0;
        }

        public int getCountOfUses() {
            return 0;
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof MockServiceTicket && ((MockServiceTicket) other).getId().equals(id);
        }
        
        @Override
        public int hashCode() {
            final HashCodeBuilder bldr = new HashCodeBuilder(17, 33);
            return bldr.append(this.id)
                       .toHashCode();
        }
    }

    static class MockTicketGrantingTicket implements TicketGrantingTicket {

        private static final long serialVersionUID = 4829406617873497061L;

        private final String id;

        private int usageCount;

        private Service proxiedBy;

        private final Date creationDate = new Date();

        private final Authentication authentication;

        /** Factory to create the principal type. **/
        @NotNull
        private final PrincipalFactory principalFactory = new DefaultPrincipalFactory();

        /** Constructor for serialization support. */
        MockTicketGrantingTicket() {
            this.id = null;
            this.authentication = null;
        }

        MockTicketGrantingTicket(final String id, final Credential credential, final Map<String, Object> principalAttributes) {
            this.id = id;
            final CredentialMetaData credentialMetaData = new BasicCredentialMetaData(credential);
            final DefaultAuthenticationBuilder builder = new DefaultAuthenticationBuilder();
            builder.setPrincipal(this.principalFactory.createPrincipal(USERNAME, principalAttributes));
            builder.setAuthenticationDate(new Date());
            builder.addCredential(credentialMetaData);
            builder.addAttribute(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, Boolean.TRUE);
            final AuthenticationHandler handler = new MockAuthenticationHandler();
            try {
                builder.addSuccess(handler.getName(), handler.authenticate(credential));
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            builder.addFailure(handler.getName(), FailedLoginException.class);
            this.authentication = builder.build();
        }

        @Override
        public Authentication getAuthentication() {
            return this.authentication;
        }

        @Override
        public List<Authentication> getSupplementalAuthentications() {
            return Collections.emptyList();
        }

        @Override
        public ServiceTicket grantServiceTicket(
                final String id,
                final Service service,
                final ExpirationPolicy expirationPolicy,
                final boolean credentialsProvided) {
            this.usageCount++;
            return new MockServiceTicket(id);
        }

        @Override
        public Service getProxiedBy() {
            return proxiedBy;
        }

        @Override
        public Map<String, Service> getServices() {
            return Collections.emptyMap();
        }

        @Override
        public void removeAllServices() {}

        @Override
        public void markTicketExpired() {}

        @Override
        public boolean isRoot() {
            return true;
        }

        @Override
        public TicketGrantingTicket getRoot() {
            return this;
        }

        @Override
        public List<Authentication> getChainedAuthentications() {
            return Collections.emptyList();
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public TicketGrantingTicket getGrantingTicket() {
            return this;
        }

        @Override
        public long getCreationTime() {
            return this.creationDate.getTime();
        }

        @Override
        public int getCountOfUses() {
            return this.usageCount;
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof MockTicketGrantingTicket
                    && ((MockTicketGrantingTicket) other).getId().equals(this.id)
                    && ((MockTicketGrantingTicket) other).getCountOfUses() == this.usageCount
                    && ((MockTicketGrantingTicket) other).getCreationTime() == this.creationDate.getTime()
                    && ((MockTicketGrantingTicket) other).getAuthentication().equals(this.authentication);
        }

        @Override
        public int hashCode() {
            final HashCodeBuilder bldr = new HashCodeBuilder(17, 33);
            return bldr.append(this.id)
                        .append(this.usageCount)
                        .append(this.creationDate.getTime())
                        .append(this.authentication).toHashCode();
        }
    }

    public static class MockAuthenticationHandler implements AuthenticationHandler {

        @Override
        public DefaultHandlerResult authenticate(final Credential credential) throws GeneralSecurityException, PreventedException {
            if (credential instanceof HttpBasedServiceCredential) {
                return new DefaultHandlerResult(this, (HttpBasedServiceCredential) credential);
            } else {
                return new DefaultHandlerResult(this, new BasicCredentialMetaData(credential));
            }
        }

        @Override
        public boolean supports(final Credential credential) {
            return true;
        }

        @Override
        public String getName() {
            return "MockAuthenticationHandler";
        }
    }
}
