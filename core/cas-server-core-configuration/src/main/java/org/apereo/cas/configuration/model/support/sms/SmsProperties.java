package org.apereo.cas.configuration.model.support.sms;

/**
 * This is {@link SmsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SmsProperties {
    private String text;
    private String from;
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
