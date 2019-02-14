package org.apereo.cas.memcached.kryo;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.CollectionUtils;

import com.esotericsoftware.kryo.KryoException;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.AccountNotFoundException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link CasKryoTranscoder} class.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Slf4j
public class CasKryoTranscoderTests {
    private static final String ST_ID = "ST-1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890ABCDEFGHIJK";
    private static final String TGT_ID = "TGT-1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890ABCDEFGHIJK-cas1";

    private static final String USERNAME = "handymanbob";
    private static final String PASSWORD = "foo";
    private static final String NICKNAME_KEY = "nickname";
    private static final String NICKNAME_VALUE = "bob";

    private final CasKryoTranscoder transcoder;

    private final Map<String, Object> principalAttributes;

    public CasKryoTranscoderTests() {
        val classesToRegister = new ArrayList<Class>();
        classesToRegister.add(MockServiceTicket.class);
        classesToRegister.add(MockTicketGrantingTicket.class);
        this.transcoder = new CasKryoTranscoder(new CasKryoPool(classesToRegister));
        this.principalAttributes = new HashMap<>();
        this.principalAttributes.put(NICKNAME_KEY, NICKNAME_VALUE);
    }

    @Test
    public void verifyEncodeDecodeTGTImpl() {
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val bldr = new DefaultAuthenticationBuilder(new DefaultPrincipalFactory()
            .createPrincipal("user", new HashMap<>(this.principalAttributes)));
        bldr.setAttributes(new HashMap<>(this.principalAttributes));
        bldr.setAuthenticationDate(ZonedDateTime.now());
        bldr.addCredential(new BasicCredentialMetaData(userPassCredential));
        bldr.addFailure("error", new AccountNotFoundException());
        bldr.addSuccess("authn", new DefaultAuthenticationHandlerExecutionResult(
            new AcceptUsersAuthenticationHandler(""),
            new BasicCredentialMetaData(userPassCredential)));

        val expectedTGT = new TicketGrantingTicketImpl(TGT_ID,
            RegisteredServiceTestUtils.getService(),
            null, bldr.build(),
            new NeverExpiresExpirationPolicy());

        val ticket = expectedTGT.grantServiceTicket(ST_ID,
            RegisteredServiceTestUtils.getService(),
            new NeverExpiresExpirationPolicy(), false, true);
        val result1 = transcoder.encode(expectedTGT);
        val resultTicket = transcoder.decode(result1);

        assertEquals(expectedTGT, resultTicket);
        val result2 = transcoder.encode(ticket);
        val resultStTicket1 = transcoder.decode(result2);
        assertEquals(ticket, resultStTicket1);
        val resultStTicket2 = transcoder.decode(result2);
        assertEquals(ticket, resultStTicket2);
    }

    @Test
    public void verifyEncodeDecode() {
        val tgt = new MockTicketGrantingTicket(USERNAME);
        val expectedST = new MockServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), tgt);
        assertEquals(expectedST, transcoder.decode(transcoder.encode(expectedST)));

        val expectedTGT = new MockTicketGrantingTicket(USERNAME);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));

        internalProxyTest();
    }

    private void internalProxyTest() {
        val expectedTGT = new MockTicketGrantingTicket(USERNAME);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    public void verifyEncodeDecodeTGTWithUnmodifiableMap() {
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val expectedTGT =
            new MockTicketGrantingTicket(TGT_ID, userPassCredential, new HashMap<>(this.principalAttributes));
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    public void verifyEncodeDecodeTGTWithUnmodifiableList() {
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val values = new ArrayList<String>();
        values.add(NICKNAME_VALUE);
        val newAttributes = new HashMap<String, Object>();
        newAttributes.put(NICKNAME_KEY, new ArrayList<>(values));
        val expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    public void verifyEncodeDecodeTGTWithLinkedHashMap() {
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val expectedTGT =
            new MockTicketGrantingTicket(TGT_ID, userPassCredential, new LinkedHashMap<>(this.principalAttributes));
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    public void verifyEncodeDecodeTGTWithListOrderedMap() {
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, this.principalAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    public void verifyEncodeDecodeTGTWithUnmodifiableSet() {
        val newAttributes = new HashMap<String, Object>();
        newAttributes.put(NICKNAME_KEY, Collections.unmodifiableSet(CollectionUtils.wrapSet(NICKNAME_VALUE)));
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    public void verifyEncodeDecodeTGTWithSingleton() {
        val newAttributes = new HashMap<String, Object>();
        newAttributes.put(NICKNAME_KEY, Collections.singleton(NICKNAME_VALUE));
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    public void verifyEncodeDecodeTGTWithSingletonMap() {
        val newAttributes = Collections.<String, Object>singletonMap(NICKNAME_KEY, NICKNAME_VALUE);
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, null, null, false, true);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    public void verifyEncodeDecodeRegisteredService() {
        val service = RegisteredServiceTestUtils.getRegisteredService("helloworld");
        val result = transcoder.encode(service);
        assertEquals(service, transcoder.decode(result));
        assertEquals(service, transcoder.decode(result));
    }

    @Test
    public void verifySTWithServiceTicketExpirationPolicy() {

        transcoder.getKryo().getClassResolver().reset();
        val tgt = new MockTicketGrantingTicket(USERNAME);
        val expectedST = new MockServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), tgt);
        val step = new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(1, 600);
        expectedST.setExpiration(step);
        val result = transcoder.encode(expectedST);
        assertEquals(expectedST, transcoder.decode(result));
        assertEquals(expectedST, transcoder.decode(result));
    }

    @Test
    public void verifyEncodeDecodeNonRegisteredClass() {
        val tgt = new MockTicketGrantingTicket(USERNAME);
        val expectedST = new MockServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), tgt);

        val step = new UnregisteredServiceTicketExpirationPolicy(1, 600);
        expectedST.setExpiration(step);
        try {
            transcoder.encode(expectedST);
            throw new AssertionError("Unregistered class is not allowed by Kryo");
        } catch (final KryoException e) {
            LOGGER.trace(e.getMessage(), e);
        } catch (final Exception e) {
            throw new AssertionError("Unexpected exception due to not resetting Kryo between de-serializations with unregistered class.");
        }
    }

    /**
     * Class for testing Kryo unregistered class handling.
     */
    @ToString(callSuper = true)
    private static class UnregisteredServiceTicketExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {
        private static final long serialVersionUID = -1704993954986738308L;

        /**
         * Instantiates a new Service ticket expiration policy.
         *
         * @param numberOfUses        the number of uses
         * @param timeToKillInSeconds the time to kill in seconds
         */
        UnregisteredServiceTicketExpirationPolicy(final int numberOfUses, final long timeToKillInSeconds) {
            super(numberOfUses, timeToKillInSeconds);
        }
    }
}
