package org.apereo.cas.uma.web.controllers.discovery;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link UmaWellKnownEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
class UmaWellKnownEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    void verifyOp() throws Exception {
        mockMvc.perform(get(OAuth20Constants.BASE_OAUTH20_URL + "/.well-known/uma-configuration")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }
}
