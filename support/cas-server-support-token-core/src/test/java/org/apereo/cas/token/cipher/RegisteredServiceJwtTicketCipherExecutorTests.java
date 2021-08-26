package org.apereo.cas.token.cipher;

import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceJwtTicketCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Tickets")
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
}
