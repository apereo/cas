package org.apereo.cas.memcached.kryo;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.model.support.memcached.BaseMemcachedProperties;
import org.apereo.cas.memcached.MemcachedUtils;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.ProxyGrantingTicketIssuerTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.esotericsoftware.kryo.KryoException;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import javax.security.auth.login.AccountNotFoundException;
import java.io.Serial;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link CasKryoTranscoder} class.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Slf4j
@Tag("Memcached")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 11211)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreServicesAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class
})
class CasKryoTranscoderTests {
    private static final String ST_ID = "ST-1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890ABCDEFGHIJK";

    private static final String TGT_ID = "TGT-1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890ABCDEFGHIJK-cas1";

    private static final String PGT_ID = "PGT-1234567";

    private static final String PT_ID = "PT-1234567";

    private static final String USERNAME = "handymanbob";

    private static final String PASSWORD = "foo";

    private static final String NICKNAME_KEY = "nickname";

    private static final String NICKNAME_VALUE = "bob";

    private Transcoder transcoder;

    private Map<String, List<Object>> principalAttributes;

    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_SERVICE_TICKET_TRACKING)
    private TicketTrackingPolicy serviceTicketSessionTrackingPolicy;

    @Autowired
    @Qualifier("componentSerializationPlan")
    private ComponentSerializationPlan componentSerializationPlan;

    @BeforeEach
    void beforeEach() {
        val classesToRegister = new ArrayList<Class>();
        classesToRegister.add(MockServiceTicket.class);
        classesToRegister.add(MockTicketGrantingTicket.class);
        classesToRegister.addAll(componentSerializationPlan.getRegisteredClasses());

        val props = new BaseMemcachedProperties();
        this.transcoder = MemcachedUtils.newTranscoder(props, classesToRegister);
        this.principalAttributes = new HashMap<>();
        this.principalAttributes.put(NICKNAME_KEY, List.of(NICKNAME_VALUE));
        this.principalAttributes.put("geoLocationRequest", List.of(new GeoLocationRequest(1, 1)));
        this.principalAttributes.put("geoLocationResponse", List.of(new GeoLocationResponse().addAddress("England")));
    }

    @Test
    void verifyBorrowAndDecode() {
        val data = new CachedData(0, ArrayUtils.EMPTY_BYTE_ARRAY, 10);
        assertThrows(KryoException.class, () -> transcoder.decode(data));
        
        if (transcoder instanceof final CasKryoTranscoder enc) {
            assertNotNull(enc.getKryo());
        }
    }

    @Test
    void verifyRegexRegisteredService() {
        var service = RegisteredServiceTestUtils.getRegisteredService("example");
        var encoded = transcoder.encode(service);
        var decoded = transcoder.decode(encoded);
        assertEquals(service, decoded);
        service = RegisteredServiceTestUtils.getRegisteredService("example");
        val attributeReleasePolicy = new ReturnAllAttributeReleasePolicy();
        attributeReleasePolicy.setPrincipalAttributesRepository(new DefaultPrincipalAttributesRepository());
        service.setAttributeReleasePolicy(attributeReleasePolicy);
        encoded = transcoder.encode(service);
        decoded = transcoder.decode(encoded);
        assertEquals(service, decoded);
    }

    @Test
    void verifyEncodeDecodeTGTImpl() throws Throwable {
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val bldr = new DefaultAuthenticationBuilder(PrincipalFactoryUtils.newPrincipalFactory()
            .createPrincipal("user", new HashMap<>(this.principalAttributes)));
        bldr.setAttributes(new HashMap<>(this.principalAttributes));
        bldr.setAuthenticationDate(ZonedDateTime.now(ZoneId.systemDefault()));
        bldr.addCredential(userPassCredential);
        bldr.addFailure("error", new AccountNotFoundException());
        bldr.addSuccess("authn", new DefaultAuthenticationHandlerExecutionResult(
            new AcceptUsersAuthenticationHandler(StringUtils.EMPTY), userPassCredential));

        val authentication = bldr.build();
        val expectedTGT = new TicketGrantingTicketImpl(TGT_ID,
            RegisteredServiceTestUtils.getService(),
            null, authentication,
            NeverExpiresExpirationPolicy.INSTANCE);

        val serviceTicket = (ProxyGrantingTicketIssuerTicket) expectedTGT.grantServiceTicket(ST_ID,
            RegisteredServiceTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        var encoded = transcoder.encode(expectedTGT);
        var decoded = transcoder.decode(encoded);

        assertEquals(expectedTGT, decoded);
        encoded = transcoder.encode(serviceTicket);
        decoded = transcoder.decode(encoded);
        assertEquals(serviceTicket, decoded);
        decoded = transcoder.decode(encoded);
        assertEquals(serviceTicket, decoded);

        val pgt = serviceTicket.grantProxyGrantingTicket(PGT_ID, authentication,
            new HardTimeoutExpirationPolicy(100));
        encoded = transcoder.encode(pgt);
        decoded = transcoder.decode(encoded);
        assertEquals(pgt, decoded);

        val pt = pgt.grantProxyTicket(PT_ID, RegisteredServiceTestUtils.getService(),
            new HardTimeoutExpirationPolicy(100), serviceTicketSessionTrackingPolicy);
        encoded = transcoder.encode(pt);
        decoded = transcoder.decode(encoded);
        assertEquals(pt, decoded);
    }

    @Test
    void verifyEncodeDecode() {
        val tgt = new MockTicketGrantingTicket(USERNAME);
        val expectedST = new MockServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), tgt);
        assertEquals(expectedST, transcoder.decode(transcoder.encode(expectedST)));

        val expectedTGT = new MockTicketGrantingTicket(USERNAME);
        expectedTGT.grantServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), null,
            false, serviceTicketSessionTrackingPolicy);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));

        internalProxyTest();
    }

    @Test
    void verifyEncodeDecodeTGTWithUnmodifiableMap() {
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val expectedTGT =
            new MockTicketGrantingTicket(TGT_ID, userPassCredential, new HashMap<>(this.principalAttributes));
        expectedTGT.grantServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), null,
            false, serviceTicketSessionTrackingPolicy);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    void verifyEncodeDecodeTGTWithUnmodifiableList() {
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val values = new ArrayList<String>();
        values.add(NICKNAME_VALUE);
        val newAttributes = new HashMap<String, List<Object>>();
        newAttributes.put(NICKNAME_KEY, new ArrayList<>(values));
        val expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), null,
            false, serviceTicketSessionTrackingPolicy);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    void verifyEncodeDecodeTGTWithLinkedHashMap() {
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, new LinkedHashMap<>(this.principalAttributes));
        expectedTGT.grantServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), null,
            false, serviceTicketSessionTrackingPolicy);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    void verifyEncodeDecodeTGTWithListOrderedMap() {
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, this.principalAttributes);
        expectedTGT.grantServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), null,
            false, serviceTicketSessionTrackingPolicy);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    void verifyEncodeDecodeTGTWithUnmodifiableSet() {
        val newAttributes = new HashMap<String, List<Object>>();
        newAttributes.put(NICKNAME_KEY, List.of(CollectionUtils.wrapSet(NICKNAME_VALUE)));

        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), null,
            false, serviceTicketSessionTrackingPolicy);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    void verifyEncodeDecodeTGTWithSingleton() {
        val newAttributes = new HashMap<String, List<Object>>();
        newAttributes.put(NICKNAME_KEY, List.of(NICKNAME_VALUE));
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), null,
            false, serviceTicketSessionTrackingPolicy);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    void verifyEncodeDecodeTGTWithSingletonMap() {
        val newAttributes = Collections.<String, List<Object>>singletonMap(NICKNAME_KEY, List.of(NICKNAME_VALUE));
        val userPassCredential = new UsernamePasswordCredential(USERNAME, PASSWORD);
        val expectedTGT = new MockTicketGrantingTicket(TGT_ID, userPassCredential, newAttributes);
        expectedTGT.grantServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), null,
            false, serviceTicketSessionTrackingPolicy);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    @Test
    void verifyEncodeDecodeRegisteredService() {
        val service = RegisteredServiceTestUtils.getRegisteredService("helloworld");
        val result = transcoder.encode(service);
        assertEquals(service, transcoder.decode(result));
        assertEquals(service, transcoder.decode(result));
    }

    @Test
    void verifySTWithServiceTicketExpirationPolicy() {
        val tgt = new MockTicketGrantingTicket(USERNAME);
        val expectedST = new MockServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), tgt);
        val step = new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(1, 600);
        expectedST.setExpirationPolicy(step);
        val result = transcoder.encode(expectedST);
        assertEquals(expectedST, transcoder.decode(result));
        assertEquals(expectedST, transcoder.decode(result));
    }

    @Test
    void verifyEncodeDecodeNonRegisteredClass() {
        val tgt = new MockTicketGrantingTicket(USERNAME);
        val expectedST = new MockServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), tgt);

        val step = new UnregisteredServiceTicketExpirationPolicy(1, 600);
        expectedST.setExpirationPolicy(step);
        try {
            transcoder.encode(expectedST);
            throw new AssertionError("Unregistered class is not allowed by Kryo");
        } catch (final KryoException e) {
            LOGGER.trace(e.getMessage(), e);
        } catch (final Exception e) {
            throw new AssertionError("Not resetting Kryo between de-serializations with unregistered class.");
        }
    }

    private void internalProxyTest() {
        val expectedTGT = new MockTicketGrantingTicket(USERNAME);
        expectedTGT.grantServiceTicket(ST_ID, RegisteredServiceTestUtils.getService(), null,
            false, serviceTicketSessionTrackingPolicy);
        val result = transcoder.encode(expectedTGT);
        assertEquals(expectedTGT, transcoder.decode(result));
        assertEquals(expectedTGT, transcoder.decode(result));
    }

    /**
     * Class for testing Kryo unregistered class handling.
     */
    @ToString(callSuper = true)
    private static final class UnregisteredServiceTicketExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {
        @Serial
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
