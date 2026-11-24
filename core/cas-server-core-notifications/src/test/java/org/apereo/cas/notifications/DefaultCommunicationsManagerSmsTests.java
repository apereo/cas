package org.apereo.cas.notifications;

import module java.base;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.notifications.sms.SmsSender;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultCommunicationsManagerSmsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("SMS")
class DefaultCommunicationsManagerSmsTests {

    @Test
    void verifySmsSentToEveryRecipient() throws Throwable {
        val sent = new ArrayList<String>();
        val smsSender = new SmsSender() {
            @Override
            public boolean send(final String from, final String to, final String message) {
                sent.add(to);
                return true;
            }
        };
        val manager = new DefaultCommunicationsManager(smsSender, null, null, null, mock(TenantExtractor.class));
        val request = SmsRequest.builder()
            .from("3477464665")
            .to(List.of("1234567890", "0987654321"))
            .text("Token: 123456")
            .build();
        assertTrue(manager.sms(request));
        assertEquals(List.of("1234567890", "0987654321"), sent);
    }

    @Test
    void verifySmsSucceedsWhenOneRecipientFails() throws Throwable {
        val sent = new ArrayList<String>();
        val smsSender = new SmsSender() {
            @Override
            public boolean send(final String from, final String to, final String message) {
                sent.add(to);
                return !"1234567890".equals(to);
            }
        };
        val manager = new DefaultCommunicationsManager(smsSender, null, null, null, mock(TenantExtractor.class));
        val request = SmsRequest.builder()
            .from("3477464665")
            .to(List.of("1234567890", "0987654321"))
            .text("Token: 123456")
            .build();
        assertTrue(manager.sms(request));
        assertEquals(List.of("1234567890", "0987654321"), sent);
    }

    @Test
    void verifySmsFailsWhenEveryRecipientFails() throws Throwable {
        val smsSender = new SmsSender() {
            @Override
            public boolean send(final String from, final String to, final String message) {
                return false;
            }
        };
        val manager = new DefaultCommunicationsManager(smsSender, null, null, null, mock(TenantExtractor.class));
        val request = SmsRequest.builder()
            .from("3477464665")
            .to(List.of("1234567890", "0987654321"))
            .text("Token: 123456")
            .build();
        assertFalse(manager.sms(request));
    }
}
