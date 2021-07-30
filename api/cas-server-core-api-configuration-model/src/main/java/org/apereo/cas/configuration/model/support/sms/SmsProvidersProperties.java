package org.apereo.cas.configuration.model.support.sms;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link SmsProvidersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-util", automated = true)
@Accessors(chain = true)
public class SmsProvidersProperties implements Serializable {
    private static final long serialVersionUID = -3713886839517507306L;

    /**
     * Twilio settings.
     */
    @NestedConfigurationProperty
    private TwilioProperties twilio = new TwilioProperties();

    /**
     * TextMagic settings.
     */
    @NestedConfigurationProperty
    private TextMagicProperties textMagic = new TextMagicProperties();

    /**
     * Clickatell settings.
     */
    @NestedConfigurationProperty
    private ClickatellProperties clickatell = new ClickatellProperties();

    /**
     * SmsMode settings.
     */
    @NestedConfigurationProperty
    private SmsModeProperties smsMode = new SmsModeProperties();

    /**
     * SNS settings.
     */
    @NestedConfigurationProperty
    private AmazonSnsProperties sns = new AmazonSnsProperties();

    /**
     * Nexmo settings.
     */
    @NestedConfigurationProperty
    private NexmoProperties nexmo = new NexmoProperties();

    /**
     * Groovy script for sending sms notifications.
     */
    @NestedConfigurationProperty
    private GroovySmsProperties groovy = new GroovySmsProperties();

    /**
     * Send SMS via via REST.
     */
    @NestedConfigurationProperty
    private RestfulSmsProperties rest = new RestfulSmsProperties();
}
