package org.apereo.cas.ticket.device;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.registry.TicketCompactor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DeviceTokenCompactorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OAuthToken")
class OAuth20DeviceTokenCompactorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauth20DeviceTokenTicketCompactor")
    private TicketCompactor<OAuth20DeviceToken> oauth20DeviceTokenTicketCompactor;

    @ParameterizedTest
    @MethodSource("codeProvider")
    void verifyOperation(final Service service, final String clientId) throws Throwable {
        val registeredService = getRegisteredService(service.getId(), clientId, "secret-at");
        servicesManager.save(registeredService);
        val deviceCode = defaultDeviceTokenFactory.createDeviceCode(service);
        deviceCode.setUserCode(UUID.randomUUID().toString());
        assertSame(OAuth20DeviceToken.class, oauth20DeviceTokenTicketCompactor.getTicketType());
        val compacted = oauth20DeviceTokenTicketCompactor.compact(deviceCode);
        assertNotNull(compacted);
        val result = (OAuth20DeviceToken) oauth20DeviceTokenTicketCompactor.expand(compacted);
        assertNotNull(result);
        assertEquals(result.getUserCode(), deviceCode.getUserCode());
    }

    static Stream<Arguments> codeProvider() {
        val service = RegisteredServiceTestUtils.getService("https://code.oauth.org");
        return Stream.of(Arguments.of(service, UUID.randomUUID().toString()));
    }
}
