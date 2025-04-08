package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.config.CasSamlIdentityProviderDiscoveryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.saml2.BaseSaml2DelegatedAuthenticationTests;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link SamlIdentityProviderDiscoveryFeedControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class,
    CasSamlIdentityProviderDiscoveryAutoConfiguration.class
},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class SamlIdentityProviderDiscoveryFeedControllerTests {

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Test
    void verifyFeed() throws Throwable {
        var mv = mockMvc.perform(get(SamlIdentityProviderDiscoveryFeedController.BASE_ENDPOINT_IDP_DISCOVERY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getModelAndView();
        assertNotNull(mv.getModel());
        assertFalse(mv.getModel().isEmpty());

        mv = mockMvc.perform(get(SamlIdentityProviderDiscoveryFeedController.BASE_ENDPOINT_IDP_DISCOVERY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getModelAndView();
        assertNotNull(mv.getModel());
        assertFalse(mv.getModel().isEmpty());

        mockMvc.perform(get(SamlIdentityProviderDiscoveryFeedController.BASE_ENDPOINT_IDP_DISCOVERY + "/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("entityID", "https://cas.example.org/idp")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(1)));
        assertNotNull(mv.getModel());
        assertFalse(mv.getModel().isEmpty());

        mv = mockMvc.perform(get(SamlIdentityProviderDiscoveryFeedController.BASE_ENDPOINT_IDP_DISCOVERY + "/redirect")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("entityID", "https://cas.example.org/idp")
            )
            .andExpect(status().isMovedTemporarily())
            .andReturn()
            .getModelAndView();
        assertNotNull(mv.getView());

        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        policy.setAllowedProviders(List.of("OtherClient"));
        policy.setPermitUndefined(false);
        accessStrategy.setDelegatedAuthenticationPolicy(policy);
        val service = RegisteredServiceTestUtils.getRegisteredService("https://service.example");
        service.setAccessStrategy(accessStrategy);
        servicesManager.save(service);

        mockMvc.perform(get(SamlIdentityProviderDiscoveryFeedController.BASE_ENDPOINT_IDP_DISCOVERY + "/redirect")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam(CasProtocolConstants.PARAMETER_SERVICE, "https://service.example")
                .queryParam("entityID", "https://cas.example.org/idp")
            )
            .andExpect(status().isForbidden());
    }
}
