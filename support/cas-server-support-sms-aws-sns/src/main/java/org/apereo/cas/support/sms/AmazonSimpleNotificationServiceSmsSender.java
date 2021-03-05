package org.apereo.cas.support.sms;

import org.apereo.cas.configuration.model.support.sms.AmazonSnsProperties;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.util.LoggingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.HashMap;

/**
 * This is {@link AmazonSimpleNotificationServiceSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class AmazonSimpleNotificationServiceSmsSender implements SmsSender {
    private final SnsClient snsClient;

    private final AmazonSnsProperties snsProperties;

    @Override
    public boolean send(final String from, final String to, final String message) {
        try {
            val smsAttributes = new HashMap<String, MessageAttributeValue>();
            if (StringUtils.isNotBlank(snsProperties.getSenderId())) {
                smsAttributes.put("AWS.SNS.SMS.SenderID", MessageAttributeValue.builder().stringValue(snsProperties.getSenderId()).dataType("String").build());
            }
            if (StringUtils.isNotBlank(snsProperties.getMaxPrice())) {
                smsAttributes.put("AWS.SNS.SMS.MaxPrice", MessageAttributeValue.builder().stringValue(snsProperties.getMaxPrice()).dataType("Number").build());
            }
            if (StringUtils.isNotBlank(snsProperties.getSmsType())) {
                smsAttributes.put("AWS.SNS.SMS.SMSType", MessageAttributeValue.builder().stringValue(snsProperties.getSmsType()).dataType("String").build());
            }
            val result = snsClient.publish(PublishRequest.builder()
                .message(message)
                .phoneNumber(to)
                .messageAttributes(smsAttributes)
                .build());
            LOGGER.debug("Submitted SMS publish request with resulting message id [{}]", result.messageId());
            return StringUtils.isNotBlank(result.messageId());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }
}


