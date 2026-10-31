package org.apereo.cas.support.saml.web.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    void verifyServiceIdentifierTooLarge() throws Throwable {
        mockMvc.perform(get(SamlIdPConstants.ENDPOINT_IDP_METADATA)
                .param("service", "99999999999999999999"))
            .andExpect(status().isOk());
    }

    @Test
    void verifyNoServiceOperation() throws Throwable {
        mockMvc.perform(get(SamlIdPConstants.ENDPOINT_IDP_METADATA)).andExpect(status().isOk());
    }

    @Test
    void verifyContentNegotiationAndConditionalRequests() throws Throwable {
        val metadataResult = mockMvc.perform(get(SamlIdPConstants.ENDPOINT_IDP_METADATA))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache"))
            .andExpect(header().exists(HttpHeaders.ETAG))
            .andReturn();
        val etag = metadataResult.getResponse().getHeader(HttpHeaders.ETAG);

        mockMvc.perform(get(SamlIdPConstants.ENDPOINT_IDP_METADATA).accept("application/samlmetadata+xml"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/samlmetadata+xml"))
            .andExpect(header().string(HttpHeaders.ETAG, etag));

        mockMvc.perform(get(SamlIdPConstants.ENDPOINT_IDP_METADATA).header(HttpHeaders.IF_NONE_MATCH, etag))
            .andExpect(status().isNotModified())
            .andExpect(content().string(StringUtils.EMPTY));
        mockMvc.perform(get(SamlIdPConstants.ENDPOINT_IDP_METADATA).header(HttpHeaders.IF_NONE_MATCH, "\"another-document\""))
            .andExpect(status().isOk());
    }
}
