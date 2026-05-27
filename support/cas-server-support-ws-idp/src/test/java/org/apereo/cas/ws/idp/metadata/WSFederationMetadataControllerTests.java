package org.apereo.cas.ws.idp.metadata;

import module java.base;
import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.ws.idp.WSFederationConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link WSFederationMetadataControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WSFederation")
class WSFederationMetadataControllerTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Test
    void verifyOperation() throws Throwable {
        mockMvc.perform(get(WSFederationConstants.ENDPOINT_FEDERATION_METADATA))
            .andExpect(status().isOk());
    }

    @Test
    void verifyFailsOperation() throws Throwable {
        try (val metadataWriter = mockStatic(WSFederationMetadataWriter.class)) {
            metadataWriter.when(() -> WSFederationMetadataWriter.produceMetadataDocument(any())).thenThrow(new RuntimeException());
            mockMvc.perform(get(WSFederationConstants.ENDPOINT_FEDERATION_METADATA))
                .andExpect(status().isInternalServerError());
        }
    }
}
