package org.apereo.cas.support.saml.web.idp.profile.sso;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link SSOSamlIdPPostProfileHandlerEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
@TestPropertySource(properties = {
    "management.endpoints.web.exposure.include=*",
    "management.endpoint.samlPostProfileResponse.access=UNRESTRICTED"
})
class SSOSamlIdPPostProfileHandlerEndpointTests extends BaseSamlIdPConfigurationTests {
    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    void beforeEach() {
        this.samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        servicesManager.save(samlRegisteredService);
    }

    @Test
    void verifyPostOperation() throws Throwable {
        mockMvc.perform(post("/actuator/samlPostProfileResponse")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_XML)
                .param("username", "casuser")
                .param("password", "casuser")
                .param("entityId", samlRegisteredService.getServiceId())
                .param("encrypt", "false"))
            .andExpect(status().isOk());
    }

    @Test
    void verifyPostLogoutOperation() throws Throwable {
        mockMvc.perform(post("/actuator/samlPostProfileResponse/logout/post")
                .with(csrf())
                .param("entityId", samlRegisteredService.getServiceId())
                .accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk());
    }

    @Test
    void verifyPostOperationWithoutPassword() throws Throwable {
        mockMvc.perform(post("/actuator/samlPostProfileResponse")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_XML)
                .param("username", "casuser")
                .param("password", StringUtils.EMPTY)
                .param("entityId", samlRegisteredService.getServiceId())
                .param("encrypt", "false"))
            .andExpect(status().isOk());
    }

    @Test
    void verifyBadCredentials() throws Throwable {
        mockMvc.perform(post("/actuator/samlPostProfileResponse")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_XML)
                .param("username", "xyz")
                .param("password", "123")
                .param("entityId", samlRegisteredService.getServiceId())
                .param("encrypt", "false"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyMissingEntity() throws Throwable {
        mockMvc.perform(post("/actuator/samlPostProfileResponse")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_XML)
                .param("username", "xyz")
                .param("password", "123")
                .param("encrypt", "false"))
            .andExpect(status().isBadRequest());
    }
}
