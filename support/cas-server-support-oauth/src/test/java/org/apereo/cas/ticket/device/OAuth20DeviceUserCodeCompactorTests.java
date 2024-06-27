package org.apereo.cas.ticket.device;

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
import java.util.UUID;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DeviceUserCodeCompactorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OAuthToken")
class OAuth20DeviceUserCodeCompactorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauth20DeviceUserCodeTicketCompactor")
    private TicketCompactor<OAuth20DeviceUserCode> oauth20DeviceUserCodeTicketCompactor;
    
    @ParameterizedTest
    @MethodSource("codeProvider")
    void verifyOperation(final Service service, final String clientId) throws Throwable {
        val registeredService = getRegisteredService(service.getId(), clientId, "secret-at");
        servicesManager.save(registeredService);

        val userCode = defaultDeviceUserCodeFactory.createDeviceUserCode(service);
        assertSame(OAuth20DeviceUserCode.class, oauth20DeviceUserCodeTicketCompactor.getTicketType());
        val compacted = oauth20DeviceUserCodeTicketCompactor.compact(userCode);
        assertNotNull(compacted);
        val result = (OAuth20DeviceUserCode) oauth20DeviceUserCodeTicketCompactor.expand(compacted);
        assertNotNull(result);
        assertEquals(result.getId(), userCode.getId());
    }

    static Stream<Arguments> codeProvider() {
        val service = RegisteredServiceTestUtils.getService("https://code.oauth.org");
        return Stream.of(Arguments.of(service, UUID.randomUUID().toString()));
    }
}
