package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.pac4j.web.DelegatedOidcFederationEntityStatementController;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.WebContext;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.federation.entity.DefaultEntityConfigurationGenerator;
import org.pac4j.oidc.federation.entity.EntityConfigurationGenerator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private static final String ENDPOINT = "/rp/" + CLIENT_NAME + "/.well-known/openid-federation";

    private DelegatedIdentityProviders identityProviders;

    private DelegatedOidcFederationEntityStatementController controller;

    private OidcConfiguration oidcConfig;

    private OidcClient oidcClient;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        identityProviders = mock(DelegatedIdentityProviders.class);
        controller = new DelegatedOidcFederationEntityStatementController(identityProviders);
        oidcConfig = new OidcConfiguration();
        oidcClient = new OidcClient(oidcConfig);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void verifyMissingClient() throws Exception {
        doReturn(Optional.empty()).when(identityProviders).findClient(eq(CLIENT_NAME), any(WebContext.class));
        mockMvc.perform(get(ENDPOINT))
            .andExpect(status().isNotFound())
            .andExpect(content().string(StringUtils.EMPTY));
    }

    @Test
    void verifyNonFederatedClient() throws Exception {
        doReturn(Optional.of(oidcClient)).when(identityProviders).findClient(eq(CLIENT_NAME), any(WebContext.class));
        mockMvc.perform(get(ENDPOINT))
            .andExpect(status().isNotFound());
    }

    @Test
    void verifyFederatedClient() throws Exception {
        oidcConfig.getFederation().setTargetOp("https//targetop");
        val entityConfigurationGenerator = mock(EntityConfigurationGenerator.class);
        oidcConfig.getFederation().setEntityConfigurationGenerator(entityConfigurationGenerator);
        when(entityConfigurationGenerator.generateEntityStatement()).thenReturn(FAKE_ENTITY_STATEMENT);
        doReturn(Optional.of(oidcClient)).when(identityProviders).findClient(eq(CLIENT_NAME), any(WebContext.class));

        mockMvc.perform(get(ENDPOINT))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(DefaultEntityConfigurationGenerator.CONTENT_TYPE))
            .andExpect(content().string(FAKE_ENTITY_STATEMENT));
    }
}
