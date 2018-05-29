package org.apereo.cas.support.sms;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.apereo.cas.configuration.model.support.sms.AmazonSnsProperties;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AmazonSimpleNotificationServiceSmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class AmazonSimpleNotificationServiceSmsSenderTests {
    @Test
    public void verifyAction() {
        final var snsClient = mock(AmazonSNS.class);
        final var result = new PublishResult();
        result.setMessageId("PASSED");
        when(snsClient.publish(any(PublishRequest.class))).thenReturn(result);
        final var properties = new AmazonSnsProperties();
        properties.setMaxPrice("100");
        properties.setSenderId("SenderId");
        final var sender = new AmazonSimpleNotificationServiceSmsSender(snsClient, properties);
        assertTrue(sender.send("1234567890", "1234567890", "TestMessage"));
    }
}
