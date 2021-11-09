package org.apereo.cas.configuration.model.support.email;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * This is {@link EmailProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-util", automated = true)
@Accessors(chain = true)
@JsonFilter("EmailProperties")
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
     * Could be plain text or a reference
     * to an external file that would serve as a template.
     *
     * If specified as a path to an external file with an extension {@code .gtemplate},
     * then the email message body would be processed using the Groovy template engine.
     * The template engine uses JSP style &lt;% %&gt; script and &lt;%= %&gt; expression syntax or
     * GString style expressions. The variable {@code out} is bound to
     * the writer that the template is being written to.
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
    @ExpressionLanguageCapable
    private String subject;

    /**
     * Email CC address, if any.
     */
    private String cc;

    /**
     * Email BCC address, if any.
     */
    private String bcc;

    /**
     * Email Reply-To address, if any.
     */
    private String replyTo;

    /**
     * Indicate whether the message body
     * should be evaluated as HTML text.
     */
    private boolean html;

    /**
     * Set whether to validate all addresses which get passed to this helper.
     */
    private boolean validateAddresses;

    /**
     * Indicate whether email settings are defined.
     *
     * @return true if undefined, false otherwise.
     */
    public boolean isUndefined() {
        return StringUtils.isBlank(text) || StringUtils.isBlank(from) || StringUtils.isBlank(subject);
    }

    /**
     * Is text/from/subject defined.
     *
     * @return true/false
     */
    public boolean isDefined() {
        return !isUndefined();
    }
}
