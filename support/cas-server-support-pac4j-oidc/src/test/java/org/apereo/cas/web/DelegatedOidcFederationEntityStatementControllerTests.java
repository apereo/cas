package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.pac4j.web.DelegatedOidcFederationEntityStatementController;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.WebContext;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.federation.entity.EntityConfigurationGenerator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedOidcFederationEntityStatementControllerTests}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Tag("Delegation")
class DelegatedOidcFederationEntityStatementControllerTests {

    private static final String CLIENT_NAME = "OidcClient";
    private static final String FAKE_ENTITY_STATEMENT = "fake-entity-statement";

    private DelegatedIdentityProviders identityProviders;

    private DelegatedOidcFederationEntityStatementController controller;

    private OidcConfiguration oidcConfig;

    private OidcClient oidcClient;

    @BeforeEach
    public void setUp() {
        identityProviders = mock(DelegatedIdentityProviders.class);
        controller = new DelegatedOidcFederationEntityStatementController(identityProviders);
        oidcConfig = new OidcConfiguration();
        oidcClient = new OidcClient(oidcConfig);
    }

    @Test
    void verifyMissingClient() {
        doReturn(Optional.empty()).when(identityProviders).findClient(eq(CLIENT_NAME), any(WebContext.class));

        val result = controller.getOpenIdFederationEndpoint(CLIENT_NAME, new MockHttpServletRequest(), new MockHttpServletResponse());
        assertTrue(result.isEmpty());
    }

    @Test
    void verifyNonFederatedClient() {
        doReturn(Optional.of(oidcClient)).when(identityProviders).findClient(eq(CLIENT_NAME), any(WebContext.class));

        val result = controller.getOpenIdFederationEndpoint(CLIENT_NAME, new MockHttpServletRequest(), new MockHttpServletResponse());
        assertTrue(result.isEmpty());
    }

    @Test
    void verifyFederatedClient() {
        oidcConfig.getFederation().setTargetOp("https//targetop");
        val entityConfigurationGenerator = mock(EntityConfigurationGenerator.class);
        oidcConfig.getFederation().setEntityConfigurationGenerator(entityConfigurationGenerator);
        when(entityConfigurationGenerator.generate()).thenReturn(FAKE_ENTITY_STATEMENT);
        doReturn(Optional.of(oidcClient)).when(identityProviders).findClient(eq(CLIENT_NAME), any(WebContext.class));

        val result = controller.getOpenIdFederationEndpoint(CLIENT_NAME, new MockHttpServletRequest(), new MockHttpServletResponse());
        assertEquals(FAKE_ENTITY_STATEMENT, result);
    }
}
