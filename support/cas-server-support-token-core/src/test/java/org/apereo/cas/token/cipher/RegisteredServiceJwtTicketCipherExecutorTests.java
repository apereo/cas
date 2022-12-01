package org.apereo.cas.token.cipher;

import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

import com.nimbusds.jwt.JWTParser;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceJwtTicketCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Cipher")
public class RegisteredServiceJwtTicketCipherExecutorTests {
    @Test
    public void verifyCipheredTokenWithoutService() {
        val c = new RegisteredServiceJwtTicketCipherExecutor();
        val token = c.encode("Value", Optional.empty());
        assertEquals("Value", c.decode(token, Optional.empty()));
    }

    @Test
    public void verifyCipherStrategyPerService() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val c = new RegisteredServiceJwtTicketCipherExecutor();
        assertTrue(c.getCipherOperationsStrategyType(service).isEmpty());

        service.getProperties().put(RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_CIPHER_STRATEGY_TYPE.getPropertyName(),
            new DefaultRegisteredServiceProperty(BaseStringCipherExecutor.CipherOperationsStrategyType.SIGN_AND_ENCRYPT.name()));
        assertTrue(c.getCipherOperationsStrategyType(service).isPresent());
    }

    @Test
    public void verifyCipherSigningAlgPerService() throws ParseException {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val c = new RegisteredServiceJwtTicketCipherExecutor();

        val properties = service.getProperties();
        val hs256 = "HS256";
        properties.put(RegisteredServiceProperties.TOKEN_SIGNING_ALG.getPropertyName(),
            new DefaultRegisteredServiceProperty(hs256));
        properties.put(RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_ENABLED.getPropertyName(),
            new DefaultRegisteredServiceProperty("true"));
        properties.put(RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_KEY.getPropertyName(),
            new DefaultRegisteredServiceProperty("qeALfMKRSME3mkHy0Qis6mhbGQFzps0ZiU-qyjsPOq_tYyR4fk2uAQR3wZfYTAlGGO3yhpJAMsq2JufeEC4fQg"));

        val payload = "{\"jwtId\": \"ST-123456\"}";
        val token = c.encode(payload, Optional.of(service));

        val jwt = JWTParser.parse(token);
        assertEquals(hs256, jwt.getHeader().getAlgorithm().getName());

        assertEquals(payload, c.decode(token, Optional.of(service)));
    }

}
