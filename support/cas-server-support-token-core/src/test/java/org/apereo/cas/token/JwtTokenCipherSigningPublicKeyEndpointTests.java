package org.apereo.cas.token;

import org.apereo.cas.config.TokenCoreConfiguration;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JwtTokenCipherSigningPublicKeyEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    AbstractCasEndpointTests.SharedTestConfiguration.class,
    TokenCoreConfiguration.class
},
    properties = {
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.jwtTicketSigningPublicKey.enabled=true"
    })
@Tag("Simple")
public class JwtTokenCipherSigningPublicKeyEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("jwtTokenCipherSigningPublicKeyEndpoint")
    private JwtTokenCipherSigningPublicKeyEndpoint endpoint;

    @Test
    public void verifyOperation() throws Exception {
        val service = RegisteredServiceTestUtils.getService("https://publickey.service");
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());

        val signingKey = new DefaultRegisteredServiceProperty();
        signingKey.addValue("classpath:/jwtRS256.key");
        registeredService.getProperties().put(
            RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_KEY.getPropertyName(), signingKey);
        servicesManager.save(registeredService);
        assertNotNull(endpoint.fetchPublicKey(service.getId()));
    }
}
