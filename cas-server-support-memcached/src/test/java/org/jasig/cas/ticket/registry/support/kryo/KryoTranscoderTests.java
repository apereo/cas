package org.jasig.cas.ticket.registry.support.kryo;

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
import org.jasig.cas.services.TestUtils;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DateTimeUtils;

import net.spy.memcached.CachedData;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

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

    private KryoTranscoder transcoder;

    private Map<String, Object> principalAttributes;

    public KryoTranscoderTests() {
        transcoder = new KryoTranscoder();
        transcoder.getKryo().register(MockServiceTicket.class);
        transcoder.getKryo().register(MockTicketGrantingTicket.class);
        transcoder.initialize();

        this.principalAttributes = new HashMap<>();
        this.principalAttributes.put(NICKNAME_KEY, NICKNAME_VALUE);
    }

    @Test
    public void verifyEncodeDecodeTGTImpl() throws Exception {
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final AuthenticationBuilder bldr = new DefaultAuthenticationBuilder(
                new DefaultPrincipalFactory()
                        .createPrincipal("user", new HashMap<>(this.principalAttributes)));
        bldr.setAttributes(new HashMap<>(this.principalAttributes));
        bldr.setAuthenticationDate(ZonedDateTime.now(ZoneOffset.UTC));
        bldr.addCredential(new BasicCredentialMetaData(userPassCredential));
        bldr.addFailure("error", AccountNotFoundException.class);
        bldr.addSuccess("authn", new DefaultHandlerResult(
                new AcceptUsersAuthenticationHandler(),
                new BasicCredentialMetaData(userPassCredential)));

        final TicketGrantingTicket expectedTGT =
                new TicketGrantingTicketImpl(TGT_ID,
                        org.jasig.cas.services.TestUtils.getService(),
                        null, bldr.build(),
                        new NeverExpiresExpirationPolicy());

        final ServiceTicket ticket = expectedTGT.grantServiceTicket(ST_ID,
                org.jasig.cas.services.TestUtils.getService(),
                new NeverExpiresExpirationPolicy(), false, true);
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
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));

        internalProxyTest("http://localhost");
        internalProxyTest("https://localhost:8080/path/file.html?p1=v1&p2=v2#fragment");
    }

    private void internalProxyTest(final String proxyUrl) throws MalformedURLException {
        final Credential proxyCredential = new HttpBasedServiceCredential(new URL(proxyUrl), TestUtils.getRegisteredService("https://.+"));
        final TicketGrantingTicket expectedTGT = new MockTicketGrantingTicket(TGT_ID, proxyCredential, this.principalAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeTGTWithUnmodifiableMap() throws Exception {
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final TicketGrantingTicket expectedTGT =
                new MockTicketGrantingTicket(TGT_ID, userPassCredential, new HashMap<>(this.principalAttributes));
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
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
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeTGTWithLinkedHashMap() throws Exception {
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final TicketGrantingTicket expectedTGT =
                new MockTicketGrantingTicket(TGT_ID, userPassCredential, new LinkedHashMap<>(this.principalAttributes));
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeTGTWithListOrderedMap() throws Exception {
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        @SuppressWarnings("unchecked")
        final TicketGrantingTicket expectedTGT =
                new MockTicketGrantingTicket(TGT_ID, userPassCredential, this.principalAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
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
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeTGTWithSingleton() throws Exception {
        final Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put(NICKNAME_KEY, Collections.singleton(NICKNAME_VALUE));
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final TicketGrantingTicket expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeTGTWithSingletonMap() throws Exception {
        final Map<String, Object> newAttributes = Collections.singletonMap(NICKNAME_KEY, (Object) NICKNAME_VALUE);
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final TicketGrantingTicket expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeRegisteredService() throws Exception {
        final RegisteredService service = TestUtils.getRegisteredService("helloworld");
        assertEquals(service, transcoder.decode(transcoder.encode(service)));
    }

    private static class MockServiceTicket implements ServiceTicket {

        private static final long serialVersionUID = -206395373480723831L;
        private String id;

        MockServiceTicket() { /* for serialization */ }

        MockServiceTicket(final String id) {
            this.id = id;
        }

        @Override
        public Service getService() {
            return null;
        }

        @Override
        public boolean isFromNewLogin() {
            return false;
        }

        @Override
        public boolean isValidFor(final Service service) {
            return false;
        }

        @Override
        public ProxyGrantingTicket grantProxyGrantingTicket(final String id, final Authentication authentication,
                                                            final ExpirationPolicy expirationPolicy) {
            return null;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public TicketGrantingTicket getGrantingTicket() {
            return null;
        }

        @Override
        public ZonedDateTime getCreationTime() {
            return DateTimeUtils.zonedDateTimeOf(0);
        }

        @Override
        public int getCountOfUses() {
            return 0;
        }

        @Override
        public ExpirationPolicy getExpirationPolicy() {
            return new NeverExpiresExpirationPolicy();
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

    private static class MockTicketGrantingTicket implements TicketGrantingTicket {

        private static final long serialVersionUID = 4829406617873497061L;

        private String id;

        private int usageCount;

        private Service proxiedBy;

        private ZonedDateTime creationDate = ZonedDateTime.now(ZoneOffset.UTC);

        private Authentication authentication;

        /** Factory to create the principal type. **/
        
        private PrincipalFactory principalFactory = new DefaultPrincipalFactory();

        /** Constructor for serialization support. */
        MockTicketGrantingTicket() {
            this.id = null;
            this.authentication = null;
        }

        MockTicketGrantingTicket(final String id, final Credential credential, final Map<String, Object> principalAttributes) {
            this.id = id;
            final CredentialMetaData credentialMetaData = new BasicCredentialMetaData(credential);
            final AuthenticationBuilder builder = new DefaultAuthenticationBuilder();
            builder.setPrincipal(this.principalFactory.createPrincipal(USERNAME, principalAttributes));
            builder.setAuthenticationDate(ZonedDateTime.now(ZoneOffset.UTC));
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
                final boolean credentialsProvided,
                final boolean onlyTrackMostRecentSession) {
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
        public Collection<ProxyGrantingTicket> getProxyGrantingTickets() {
            return Collections.emptySet();
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
        public ZonedDateTime getCreationTime() {
            return this.creationDate;
        }

        @Override
        public int getCountOfUses() {
            return this.usageCount;
        }

        @Override
        public ExpirationPolicy getExpirationPolicy() {
            return new NeverExpiresExpirationPolicy();
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof MockTicketGrantingTicket
                    && ((MockTicketGrantingTicket) other).getId().equals(this.id)
                    && ((MockTicketGrantingTicket) other).getCountOfUses() == this.usageCount
                    && ((MockTicketGrantingTicket) other).getCreationTime().equals(this.creationDate)
                    && ((MockTicketGrantingTicket) other).getAuthentication().equals(this.authentication);
        }

        @Override
        public int hashCode() {
            final HashCodeBuilder bldr = new HashCodeBuilder(17, 33);
            return bldr.append(this.id)
                        .append(this.usageCount)
                        .append(this.creationDate)
                        .append(this.authentication).toHashCode();
        }
    }

    private static class MockAuthenticationHandler implements AuthenticationHandler {

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
            return this.getClass().getSimpleName();
        }
    }
}
