package org.apereo.cas.support.saml.web.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This is {@link SamlIdPMetadataControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLMetadata")
class SamlIdPMetadataControllerTests extends BaseSamlIdPConfigurationTests {

    @Test
    void verifyOperationByServiceId() throws Throwable {
        mockMvc.perform(get(SamlIdPConstants.ENDPOINT_IDP_METADATA + "/signingCertificate"))
            .andExpect(status().isOk());
        mockMvc.perform(get(SamlIdPConstants.ENDPOINT_IDP_METADATA + "/encryptionCertificate"))
            .andExpect(status().isOk());
    }

    @Test
    void verifyOperation() throws Throwable {
        val service = SamlIdPTestUtils.getSamlRegisteredService();
        servicesManager.save(service);
        mockMvc.perform(get(SamlIdPConstants.ENDPOINT_IDP_METADATA)
                .param("service", String.valueOf(service.getId())))
            .andExpect(status().isOk());
    }

    @Test
    void verifyNoServiceOperation() throws Throwable {
        mockMvc.perform(get(SamlIdPConstants.ENDPOINT_IDP_METADATA)).andExpect(status().isOk());
    }
}
