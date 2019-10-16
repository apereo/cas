package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.DynamoDbTicketRegistryConfiguration;
import org.apereo.cas.config.DynamoDbTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.config.OAuth20ProtocolTicketCatalogConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessTokenFactory;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.refreshtoken.OAuth20DefaultRefreshTokenFactory;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("DynamoDb")
@SpringBootTest(classes = {
    DynamoDbTicketRegistryConfiguration.class,
    DynamoDbTicketRegistryTicketCatalogConfiguration.class,
    OAuth20ProtocolTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.ticket.registry.dynamoDb.endpoint=http://localhost:8000",
        "cas.ticket.registry.dynamoDb.dropTablesOnStartup=true",
        "cas.ticket.registry.dynamoDb.localInstance=true",
        "cas.ticket.registry.dynamoDb.region=us-east-1"
    })
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 8000)
public class DynamoDbTicketRegistryTests extends BaseTicketRegistryTests {
    static {
        System.setProperty("aws.accessKeyId", "AKIAIPPIGGUNIO74C63Z");
        System.setProperty("aws.secretKey", "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");
    }

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return ticketRegistry;
    }

    @RepeatedTest(2)
    public void verifyOAuthCodeCanBeAdded() {
        val code = new OAuth20DefaultOAuthCodeFactory(neverExpiresExpirationPolicyBuilder(), servicesManager)
            .create(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getAuthentication(), new MockTicketGrantingTicket("casuser"),
            CollectionUtils.wrapSet("1", "2"), "code-challenge", "code-challenge-method", "clientId1234567", new HashMap<>());
        ticketRegistry.addTicket(code);
        assertSame(1, ticketRegistry.deleteTicket(code.getId()), "Wrong ticket count");
        assertNull(ticketRegistry.getTicket(code.getId()));
    }

    @RepeatedTest(2)
    public void verifyAccessTokenCanBeAdded() {
        val jwtBuilder = new JwtBuilder("cas-prefix", CipherExecutor.noOpOfSerializableToString(),
            servicesManager, RegisteredServiceCipherExecutor.noOp());
        val token = new OAuth20DefaultAccessTokenFactory(neverExpiresExpirationPolicyBuilder(), jwtBuilder, servicesManager)
            .create(RegisteredServiceTestUtils.getService(),
                RegisteredServiceTestUtils.getAuthentication(), new MockTicketGrantingTicket("casuser"),
                CollectionUtils.wrapSet("1", "2"), "clientId1234567", new HashMap<>());
        ticketRegistry.addTicket(token);
        assertSame(1, ticketRegistry.deleteTicket(token.getId()), "Wrong ticket count");
        assertNull(ticketRegistry.getTicket(token.getId()));
    }

    @RepeatedTest(2)
    public void verifyRefreshTokenCanBeAdded() {
        val token = new OAuth20DefaultRefreshTokenFactory(neverExpiresExpirationPolicyBuilder(), servicesManager)
            .create(RegisteredServiceTestUtils.getService(),
                RegisteredServiceTestUtils.getAuthentication(), new MockTicketGrantingTicket("casuser"),
                CollectionUtils.wrapSet("1", "2"), "clientId1234567", new HashMap<>());
        ticketRegistry.addTicket(token);
        assertSame(1, ticketRegistry.deleteTicket(token.getId()), "Wrong ticket count");
        assertNull(ticketRegistry.getTicket(token.getId()));
    }
}
