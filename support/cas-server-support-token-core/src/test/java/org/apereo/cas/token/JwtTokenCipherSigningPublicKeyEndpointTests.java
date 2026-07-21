package org.apereo.cas.token;

import module java.base;
import org.apereo.cas.config.CasTokenCoreAutoConfiguration;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.io.pem.PemReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link JwtTokenCipherSigningPublicKeyEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    AbstractCasEndpointTests.SharedTestConfiguration.class,
    CasTokenCoreAutoConfiguration.class
},
    properties = {
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.jwtTicketSigningPublicKey.access=UNRESTRICTED"
    })
@Tag("ActuatorEndpoint")
@ExtendWith(CasTestExtension.class)
class JwtTokenCipherSigningPublicKeyEndpointTests extends AbstractCasEndpointTests {
    @Test
    void verifyOperationWithoutService() throws Throwable {
        mockMvc.perform(get("/actuator/jwtTicketSigningPublicKey")
                .param("service", StringUtils.EMPTY)
                .accept(MediaType.TEXT_PLAIN))
            .andExpect(status().isOk())
            .andExpect(content().string(StringUtils.EMPTY));
    }

    @Test
    void verifyOperationByService() throws Throwable {
        val service = RegisteredServiceTestUtils.getService("https://publickey.service");
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());

        val signingKey = new DefaultRegisteredServiceProperty();
        signingKey.addValue("classpath:/jwtRS256.key");
        registeredService.getProperties().put(
            RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_KEY.getPropertyName(), signingKey);
        servicesManager.save(registeredService);
        val publicKey = mockMvc.perform(get("/actuator/jwtTicketSigningPublicKey")
                .param("service", service.getId())
                .accept(MediaType.TEXT_PLAIN))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        assertNotNull(publicKey);
        try (val r = new PemReader(new StringReader(publicKey))) {
            assertNotNull(r.readPemObject());
        }
    }
}
