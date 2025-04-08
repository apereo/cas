package org.apereo.cas.web.saml2;

import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientSessionManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientSaml2SessionManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Delegation")
@SpringBootTest(classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class, properties = "cas.authn.pac4j.core.session-replication.replicate-sessions=false")
@ExtendWith(CasTestExtension.class)
class DelegatedClientSaml2SessionManagerTests {

    @Autowired
    @Qualifier("delegatedClientSaml2SessionManager")
    private DelegatedClientSessionManager delegatedClientSaml2SessionManager;

    @Autowired
    @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
    private DelegatedIdentityProviders identityProviders;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Exception {
        assertNotNull(delegatedClientSaml2SessionManager.getName());
        val context = MockRequestContext.create(applicationContext);
        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());

        val client = identityProviders.findClient("SAML2Client", webContext).orElseThrow();
        assertTrue(delegatedClientSaml2SessionManager.supports(client));
        
        val ticket = new TransientSessionTicketImpl(UUID.randomUUID().toString(),
            NeverExpiresExpirationPolicy.INSTANCE, RegisteredServiceTestUtils.getService(), Map.of());

        delegatedClientSaml2SessionManager.trackIdentifier(webContext, ticket, client);
        val clientIdentifier = delegatedClientSaml2SessionManager.retrieveIdentifier(webContext, client);
        assertEquals(ticket.getId(), clientIdentifier);
    }
}
