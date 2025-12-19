package org.apereo.cas.token.cipher;

import module java.base;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceJwtTicketCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Cipher")
class RegisteredServiceJwtTicketCipherExecutorTests {
    @Test
    void verifyCipheredTokenWithoutService() {
        val cipher = new RegisteredServiceJwtTicketCipherExecutor();
        val token = cipher.encode("Value", Optional.empty());
        assertEquals("Value", cipher.decode(token, Optional.empty()));
    }

    @Test
    void verifyCipherStrategyPerService() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val cipher = new RegisteredServiceJwtTicketCipherExecutor();
        assertTrue(cipher.getCipherOperationsStrategyType(service).isEmpty());

        service.getProperties().put(RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_CIPHER_STRATEGY_TYPE.getPropertyName(),
            new DefaultRegisteredServiceProperty(BaseStringCipherExecutor.CipherOperationsStrategyType.SIGN_AND_ENCRYPT.name()));
        assertTrue(cipher.getCipherOperationsStrategyType(service).isPresent());
    }
}
