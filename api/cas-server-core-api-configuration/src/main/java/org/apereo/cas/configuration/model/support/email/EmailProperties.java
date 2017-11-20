package org.apereo.cas.configuration.model.support.email;

import org.apereo.cas.configuration.support.RequiredProperty;

import java.io.Serializable;

/**
 * This is {@link EmailProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class EmailProperties implements Serializable {

    private static final long serialVersionUID = 7367120636536230761L;
    /**
     * Principal attribute name that indicates the destination email address
     * for this message. The attribute must already be resolved and available
     * to the CAS principal.
     */
    @RequiredProperty
    private String attributeName = "mail";

    /**
     * Email message body.
     */
    private String text;

    /**
     * Email from address.
     */
    @RequiredProperty
    private String from;

    /**
     * Email subject line.
     */
    @RequiredProperty
    private String subject;

    /**
     * Email CC address, if any.
     */
    private String cc;

    /**
     * Email BCC address, if any.
     */
    private String bcc;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(final String attributeName) {
        this.attributeName = attributeName;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(final String bcc) {
        this.bcc = bcc;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(final String cc) {
        this.cc = cc;
    }
}
