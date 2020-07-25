package org.apereo.cas.support.sms;

import org.apereo.cas.configuration.model.support.sms.AmazonSnsProperties;
import org.apereo.cas.util.io.SmsSender;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

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
    private final AmazonSNS snsClient;
    private final AmazonSnsProperties snsProperties;

    @Override
    public boolean send(final String from, final String to, final String message) {
        try {
            val smsAttributes = new HashMap<String, MessageAttributeValue>();
            if (StringUtils.isNotBlank(snsProperties.getSenderId())) {
                smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue().withStringValue(snsProperties.getSenderId()).withDataType("String"));
            }
            if (StringUtils.isNotBlank(snsProperties.getMaxPrice())) {
                smsAttributes.put("AWS.SNS.SMS.MaxPrice", new MessageAttributeValue().withStringValue(snsProperties.getMaxPrice()).withDataType("Number"));
            }
            if (StringUtils.isNotBlank(snsProperties.getSmsType())) {
                smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue().withStringValue(snsProperties.getSmsType()).withDataType("String"));
            }
            val result = snsClient.publish(new PublishRequest()
                .withMessage(message)
                .withPhoneNumber(to)
                .withMessageAttributes(smsAttributes));
            LOGGER.debug("Submitted SMS publish request with resulting message id [{}]", result.getMessageId());
            return StringUtils.isNotBlank(result.getMessageId());
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return false;
    }
}


