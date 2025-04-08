package org.apereo.cas.web.saml2;

import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link DelegatedSaml2ClientMetadataControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("SAML2Web")
class DelegatedSaml2ClientMetadataControllerTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Test
    void verifyEndpoints() throws Exception {
        assertNotNull(mockMvc.perform(get(DelegatedSaml2ClientMetadataController.BASE_ENDPOINT_SERVICE_PROVIDER + "/metadata")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());

        assertNotNull(mockMvc.perform(get(DelegatedSaml2ClientMetadataController.BASE_ENDPOINT_SERVICE_PROVIDER + "/idp/metadata")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());

        assertNotNull(mockMvc.perform(get(DelegatedSaml2ClientMetadataController.BASE_ENDPOINT_SERVICE_PROVIDER + "/SAML2Client/idp/metadata")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());

        assertNotNull(mockMvc.perform(get(DelegatedSaml2ClientMetadataController.BASE_ENDPOINT_SERVICE_PROVIDER + "/SAML2Client/metadata")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());

        assertNotNull(mockMvc.perform(get(DelegatedSaml2ClientMetadataController.BASE_ENDPOINT_SERVICE_PROVIDER + "/UnknownClient/idp/metadata")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse()
            .getContentAsString());

        assertNotNull(mockMvc.perform(get(DelegatedSaml2ClientMetadataController.BASE_ENDPOINT_SERVICE_PROVIDER + "/UnknownClient/metadata")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isNotAcceptable())
            .andReturn()
            .getResponse()
            .getContentAsString());
    }

}
