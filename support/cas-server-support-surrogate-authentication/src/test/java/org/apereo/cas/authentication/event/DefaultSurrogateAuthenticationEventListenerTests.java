package org.apereo.cas.authentication.event;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationSuccessfulEvent;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultSurrogateAuthenticationEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("Impersonation")
class DefaultSurrogateAuthenticationEventListenerTests {

    @Test
    @SuppressWarnings("unchecked")
    void verifySmsSentWithoutTenant() throws Throwable {
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getSurrogate().getSms().setFrom("3487244312");
        casProperties.getAuthn().getSurrogate().getSms().setText("Impersonation: ${details}");

        val communicationsManager = mock(CommunicationsManager.class);
        when(communicationsManager.isSmsSenderDefined()).thenReturn(true);
        when(communicationsManager.isMailSenderDefined()).thenReturn(false);
        when(communicationsManager.sms(anyList())).thenReturn(true);

        val listener = new DefaultSurrogateAuthenticationEventListener(communicationsManager,
            casProperties, mock(TenantExtractor.class));

        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        val clientInfo = ClientInfo.from(request);
        assertNull(clientInfo.getTenant());

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            Map.of("phone", List.of("1234567890")));
        val event = new CasSurrogateAuthenticationSuccessfulEvent(this, principal, "surrogate", clientInfo);
        assertDoesNotThrow(() -> listener.handleSurrogateAuthenticationSuccessEvent(event));

        val captor = ArgumentCaptor.forClass(List.class);
        verify(communicationsManager).sms(captor.capture());
        val requests = (List<SmsRequest>) captor.getValue();
        assertFalse(requests.isEmpty());
        requests.forEach(smsRequest -> {
            assertNull(smsRequest.getTenant());
            assertEquals("3487244312", smsRequest.getFrom());
            assertTrue(smsRequest.getText().startsWith("Impersonation: "));
        });
    }
}
