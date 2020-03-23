package org.apereo.cas.configuration.model.support.sms;

import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link AmazonSnsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-sms-clickatell")
@Getter
@Setter
@Accessors(chain = true)
public class AmazonSnsProperties extends BaseAmazonWebServicesProperties {
    private static final long serialVersionUID = -3366665169030844517L;

    /**
     * A custom ID that contains up to 11 alphanumeric characters, including at least one letter and no spaces.
     * The sender ID is displayed as the message sender on the receiving device. For example, you can use your
     * business brand to make the message source easier to recognize.
     * Support for sender IDs varies by country and/or region. For example, messages delivered to
     * U.S. phone numbers will not display the sender ID.
     * If you do not specify a sender ID, the message will display a long code as the sender ID in
     * supported countries and regions. For countries or regions that require an alphabetic sender ID,
     * the message displays NOTICE as the sender ID.
     */
    private String senderId;
    /**
     * The maximum amount in USD that you are willing to spend to send the SMS message.
     * Amazon SNS will not send the message if it determines that doing so would incur a cost that exceeds the maximum price.
     * This attribute has no effect if your month-to-date SMS costs have already exceeded the limit set
     * for the MonthlySpendLimit attribute, which you set by using the SetSMSAttributes request.
     * If you are sending the message to an Amazon SNS topic, the maximum price applies to each message
     * delivery to each phone number that is subscribed to the topic.
     */
    private String maxPrice;
    /**
     * The type of message that you are sending:
     * <ul>
     * <li>Promotional - Noncritical messages, such as marketing messages. Amazon SNS optimizes the message delivery to incur the lowest cost.    </li>
     * <li>Transactional – Critical messages that support customer transactions, such as one-time passcodes
     * for multi-factor authentication. Amazon SNS optimizes the message delivery to achieve the highest reliability. </li>
     * </ul>
     */
    private String smsType = "Transactional";

}
