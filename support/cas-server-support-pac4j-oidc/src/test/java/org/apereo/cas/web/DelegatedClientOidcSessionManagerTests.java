package org.apereo.cas.web;

import org.apereo.cas.config.CasDelegatedAuthenticationOidcAutoConfiguration;
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
 * This is {@link DelegatedClientOidcSessionManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@SpringBootTest(
    classes = {
        CasDelegatedAuthenticationOidcAutoConfiguration.class,
        BaseDelegatedAuthenticationTests.SharedTestConfiguration.class
    },
    properties = {
        "cas.authn.pac4j.core.session-replication.replicate-sessions=false",

        "cas.authn.pac4j.oauth2[0].id=123456",
        "cas.authn.pac4j.oauth2[0].secret=s3cr3t",
        "cas.authn.pac4j.oauth2[0].client-name=OAuth20Client",

        "cas.authn.pac4j.oidc[0].google.client-name=GoogleClient",
        "cas.authn.pac4j.oidc[0].google.id=123",
        "cas.authn.pac4j.oidc[0].google.secret=123",
        "cas.authn.pac4j.oidc[0].google.discovery-uri=https://localhost:8443/.well-known/openid-configuration"
    })
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
class DelegatedClientOidcSessionManagerTests {

    @Autowired
    @Qualifier("delegatedClientOidcSessionManager")
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


        val oauthClient = identityProviders.findClient("OAuth20Client", webContext).orElseThrow();
        val googleClient = identityProviders.findClient("GoogleClient", webContext).orElseThrow();
        assertTrue(delegatedClientSaml2SessionManager.supports(googleClient));
        assertTrue(delegatedClientSaml2SessionManager.supports(oauthClient));

        val ticket = new TransientSessionTicketImpl(UUID.randomUUID().toString(),
            NeverExpiresExpirationPolicy.INSTANCE, RegisteredServiceTestUtils.getService(), Map.of());

        delegatedClientSaml2SessionManager.trackIdentifier(webContext, ticket, googleClient);
        delegatedClientSaml2SessionManager.trackIdentifier(webContext, ticket, oauthClient);
        assertEquals(ticket.getId(), delegatedClientSaml2SessionManager.retrieveIdentifier(webContext, googleClient));
        assertEquals(ticket.getId(), delegatedClientSaml2SessionManager.retrieveIdentifier(webContext, oauthClient));
    }
}
