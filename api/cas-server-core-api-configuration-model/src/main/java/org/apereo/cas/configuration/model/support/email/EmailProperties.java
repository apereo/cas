package org.apereo.cas.configuration.model.support.email;

import org.apereo.cas.configuration.support.RequiredProperty;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link EmailProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
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
}
