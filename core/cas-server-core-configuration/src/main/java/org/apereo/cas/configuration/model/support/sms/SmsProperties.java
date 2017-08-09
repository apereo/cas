package org.apereo.cas.configuration.model.support.sms;

import java.io.Serializable;

/**
 * This is {@link SmsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SmsProperties implements Serializable {

    private static final long serialVersionUID = -3713886839517507306L;
    /**
     * The body of the SMS message.
     */
    private String text;
    /**
     * The from address for the message.
     */
    private String from;

    /**
     * Principal attribute name that indicates the destination phone number
     * for this SMS message. The attribute must already be resolved and available
     * to the CAS principal.
     */
    private String attributeName = "phone";

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(final String attributeName) {
        this.attributeName = attributeName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }
}
