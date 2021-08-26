package org.apereo.cas.support.sms;

import org.apereo.cas.configuration.model.support.sms.AmazonSnsProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AmazonSimpleNotificationServiceSmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("AmazonWebServices")
public class AmazonSimpleNotificationServiceSmsSenderTests {
    @Test
    public void verifyAction() {
        val snsClient = mock(SnsClient.class);
        val result = PublishResponse.builder().messageId("PASSED").build();
        when(snsClient.publish(any(PublishRequest.class))).thenReturn(result);
        val properties = new AmazonSnsProperties();
        properties.setMaxPrice("100");
        properties.setSenderId("SenderId");
        val sender = new AmazonSimpleNotificationServiceSmsSender(snsClient, properties);
        assertTrue(sender.send("1234567890", "1234567890", "TestMessage"));
    }
    
    @Test
    public void verifyFailsAction() {
        val snsClient = mock(SnsClient.class);
        when(snsClient.publish(any(PublishRequest.class))).thenThrow(new IllegalArgumentException());
        val properties = new AmazonSnsProperties();
        properties.setMaxPrice("100");
        properties.setSenderId("SenderId");
        val sender = new AmazonSimpleNotificationServiceSmsSender(snsClient, properties);
        assertFalse(sender.send("1234567890", "1234567890", "TestMessage"));
    }
}
