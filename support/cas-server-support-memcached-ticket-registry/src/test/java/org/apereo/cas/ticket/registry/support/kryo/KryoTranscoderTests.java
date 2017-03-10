package org.apereo.cas.ticket.registry.support.kryo;

import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import net.spy.memcached.CachedData;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HttpBasedServiceCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Test;

import javax.security.auth.login.AccountNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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

    private final KryoTranscoder transcoder;

    private final Map<String, Object> principalAttributes;

    public KryoTranscoderTests() {
        transcoder = new KryoTranscoder();
        final Map<Class<?>, Serializer> serializerMap = new HashMap<>();
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
                        .createPrincipal("user", new HashMap<>(this.principalAttributes)));
        bldr.setAttributes(new HashMap<>(this.principalAttributes));
        bldr.setAuthenticationDate(ZonedDateTime.now());
        bldr.addCredential(new BasicCredentialMetaData(userPassCredential));
        bldr.addFailure("error", AccountNotFoundException.class);
        bldr.addSuccess("authn", new DefaultHandlerResult(
                new AcceptUsersAuthenticationHandler(""),
                new BasicCredentialMetaData(userPassCredential)));

        final TicketGrantingTicket expectedTGT =
                new TicketGrantingTicketImpl(TGT_ID,
                        RegisteredServiceTestUtils.getService(),
                        null, bldr.build(),
                        new NeverExpiresExpirationPolicy());

        final ServiceTicket ticket = expectedTGT.grantServiceTicket(ST_ID,
                RegisteredServiceTestUtils.getService(),
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
        final TicketGrantingTicket tgt = new MockTicketGrantingTicket(USERNAME);
        final ServiceTicket expectedST = new MockServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), tgt);
        assertEquals(expectedST, transcoder.decode(transcoder.encode(expectedST)));

        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final TicketGrantingTicket expectedTGT = new MockTicketGrantingTicket(USERNAME);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));

        internalProxyTest("http://localhost");
        internalProxyTest("https://localhost:8080/path/file.html?p1=v1&p2=v2#fragment");
    }

    private void internalProxyTest(final String proxyUrl) throws MalformedURLException {
        final Credential proxyCredential = new HttpBasedServiceCredential(new URL(proxyUrl), RegisteredServiceTestUtils.getRegisteredService("https://.+"));
        final TicketGrantingTicket expectedTGT = new MockTicketGrantingTicket(USERNAME);
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
        final Map<String, Object> newAttributes = Collections.singletonMap(NICKNAME_KEY, NICKNAME_VALUE);
        final Credential userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        final TicketGrantingTicket expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    @Test
    public void verifyEncodeDecodeRegisteredService() throws Exception {
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService("helloworld");
        assertEquals(service, transcoder.decode(transcoder.encode(service)));
    }
}
