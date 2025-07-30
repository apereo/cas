package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.config.CasDynamoDbTicketRegistryAutoConfiguration;
import org.apereo.cas.config.CasOAuth20AutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessTokenFactory;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenFactory;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.core.SdkSystemSetting;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("DynamoDb")
@ImportAutoConfiguration({
    CasDynamoDbTicketRegistryAutoConfiguration.class,
    CasOAuth20AutoConfiguration.class
})
@TestPropertySource(
    properties = {
        "cas.ticket.registry.dynamo-db.endpoint=http://localhost:8000",
        "cas.ticket.registry.dynamo-db.drop-tables-on-startup=true",
        "cas.ticket.registry.dynamo-db.local-instance=true",
        "cas.ticket.registry.dynamo-db.region=us-east-1",
        "cas.authn.oauth.access-token.storage-name=test-oauthAccessTokensCache",
        "cas.authn.oauth.refresh-token.storage-name=test-oauthRefreshTokensCache",
        "cas.authn.oauth.code.storage-name=test-oauthCodesCache",
        "cas.authn.oauth.device-token.storage-name=test-oauthDeviceTokensCache",
        "cas.authn.oauth.device-user-code.storage-name=test-oauthDeviceUserCodesCache"
    })
@EnabledIfListeningOnPort(port = 8000)
@Getter
class DynamoDbTicketRegistryTests extends BaseTicketRegistryTests {
    private static final int COUNT = 250;

    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
    private PrincipalResolver principalResolver;

    @Autowired
    @Qualifier("defaultRefreshTokenFactory")
    private OAuth20RefreshTokenFactory defaultRefreshTokenFactory;

    @RepeatedTest(2)
    void verifyOAuthCodeCanBeAdded() throws Throwable {
        val code = createOAuthCode();
        newTicketRegistry.addTicket(code);
        assertSame(1, newTicketRegistry.deleteTicket(code.getId()), "Wrong ticket count");
        assertNull(newTicketRegistry.getTicket(code.getId()));
    }

    @RepeatedTest(2)
    void verifyAccessTokenCanBeAdded() throws Throwable {
        val code = createOAuthCode();
        val jwtBuilder = new JwtBuilder(CipherExecutor.noOpOfSerializableToString(),
            applicationContext, servicesManager, principalResolver,
            RegisteredServiceCipherExecutor.noOp(), webApplicationServiceFactory, casProperties);
        val token = new OAuth20DefaultAccessTokenFactory(
            newTicketRegistry, neverExpiresExpirationPolicyBuilder(), jwtBuilder,
            servicesManager, TicketTrackingPolicy.noOp())
            .create(RegisteredServiceTestUtils.getService(),
                RegisteredServiceTestUtils.getAuthentication(), new MockTicketGrantingTicket("casuser"),
                CollectionUtils.wrapSet("1", "2"), code.getId(), "clientId1234567", new HashMap<>(),
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        newTicketRegistry.addTicket(token);
        assertSame(1, newTicketRegistry.deleteTicket(token.getId()), "Wrong ticket count");
        assertNull(newTicketRegistry.getTicket(token.getId()));
    }

    @RepeatedTest(2)
    void verifyRefreshTokenCanBeAdded() throws Throwable {
        val token = defaultRefreshTokenFactory.create(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getAuthentication(), new MockTicketGrantingTicket("casuser"),
            CollectionUtils.wrapSet("1", "2"),
            "clientId1234567", StringUtils.EMPTY, new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        newTicketRegistry.addTicket(token);
        assertSame(1, newTicketRegistry.deleteTicket(token.getId()), "Wrong ticket count");
        assertNull(newTicketRegistry.getTicket(token.getId()));
    }

    @RepeatedTest(2)
    void verifyRegistryQuery() throws Throwable {
        val tgt = new TicketGrantingTicketImpl("TGT-115500",
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val registry = getNewTicketRegistry();
        registry.addTicket(tgt);
        assertEquals(1, registry.query(TicketRegistryQueryCriteria.builder()
            .count(1L).type(TicketGrantingTicket.PREFIX).decode(true).build()).size());
    }

    @RepeatedTest(2)
    void verifyLargeDataset() {
        val ticketGrantingTicketToAdd = Stream.generate(() -> {
                val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                    .getNewTicketId(TicketGrantingTicket.PREFIX);
                return new TicketGrantingTicketImpl(tgtId,
                    CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
            })
            .limit(COUNT);
        var stopwatch = new StopWatch();
        newTicketRegistry.addTicket(ticketGrantingTicketToAdd);
        stopwatch.start();
        assertEquals(COUNT, newTicketRegistry.stream().count());
        stopwatch.stop();
        var time = stopwatch.getTime(TimeUnit.SECONDS);
        assertTrue(time <= 20);
    }

    private OAuth20Code createOAuthCode() throws Throwable {
        return new OAuth20DefaultOAuthCodeFactory(new DefaultUniqueTicketIdGenerator(),
            neverExpiresExpirationPolicyBuilder(), servicesManager, CipherExecutor.noOpOfStringToString(),
            TicketTrackingPolicy.noOp())
            .create(RegisteredServiceTestUtils.getService(),
                RegisteredServiceTestUtils.getAuthentication(), new MockTicketGrantingTicket("casuser"),
                CollectionUtils.wrapSet("1", "2"), "code-challenge",
                "code-challenge-method", "clientId1234567", new HashMap<>(),
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
    }
}
