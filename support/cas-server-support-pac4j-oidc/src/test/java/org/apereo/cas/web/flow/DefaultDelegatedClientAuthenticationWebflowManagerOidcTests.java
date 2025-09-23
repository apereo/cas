package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.config.CasDelegatedAuthenticationOidcAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import lombok.val;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDelegatedClientAuthenticationWebflowManagerOidcTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(
    classes = {
        CasDelegatedAuthenticationOidcAutoConfiguration.class,
        BaseDelegatedAuthenticationTests.SharedTestConfiguration.class
    },
    properties = {
        "cas.authn.pac4j.core.session-replication.cookie.crypto.alg=" + ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256,
        "cas.authn.pac4j.core.session-replication.cookie.crypto.encryption.key=3RXtt06xYUAli7uU-Z915ZGe0MRBFw3uDjWgOEf1GT8",
        "cas.authn.pac4j.core.session-replication.cookie.crypto.signing.key=jIFR-fojN0vOIUcT0hDRXHLVp07CV-YeU8GnjICsXpu65lfkJbiKP028pT74Iurkor38xDGXNcXk_Y1V4rNDqw",
        "cas.authn.pac4j.cookie.enabled=true"
    })
@Tag("Webflow")
@ExtendWith(CasTestExtension.class)
class DefaultDelegatedClientAuthenticationWebflowManagerOidcTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier(DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME)
    private DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private JEEContext context;

    private MockRequestContext requestContext;
    
    @BeforeEach
    void setup() throws Exception {
        val service = RegisteredServiceTestUtils.getService();
        requestContext = MockRequestContext.create(applicationContext)
            .withUserAgent("Chrome")
            .setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        context = new JEEContext(requestContext.getHttpServletRequest(), requestContext.getHttpServletResponse());
    }

    @Test
    void verifyOidcStoreOperation() throws Throwable {
        val config = new OidcConfiguration();
        config.setClientId(UUID.randomUUID().toString());
        config.setSecret(UUID.randomUUID().toString());
        val client = new OidcClient(config);
        client.setConfiguration(config);
        val ticket = delegatedClientAuthenticationWebflowManager.store(requestContext, context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, client);
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }

    @Test
    void verifyOAuth2StoreOperation() throws Throwable {
        val config = new OAuth20Configuration();
        config.setKey(UUID.randomUUID().toString());
        config.setSecret(UUID.randomUUID().toString());
        val client = new OAuth20Client();
        client.setConfiguration(config);
        val ticket = delegatedClientAuthenticationWebflowManager.store(requestContext, context, client);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        val service = delegatedClientAuthenticationWebflowManager.retrieve(requestContext, context, client);
        assertNotNull(service);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }
}
