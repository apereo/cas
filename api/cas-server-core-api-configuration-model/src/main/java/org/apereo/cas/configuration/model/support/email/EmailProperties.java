package org.apereo.cas.configuration.model.support.email;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link EmailProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@Slf4j
@RequiresModule(name = "cas-server-core-util", automated = true)
@Accessors(chain = true)
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
     * Format body.
     *
     * @param arguments the arguments
     * @return the string
     */
    public String getFormattedBody(final Object... arguments) {
        if (StringUtils.isBlank(this.text)) {
            LOGGER.warn("No email body is defined");
            return StringUtils.EMPTY;
        }
        try {
            val templateFile = ResourceUtils.getFile(this.text);
            val contents = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);
            return String.format(contents, arguments);
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
            return String.format(this.text, arguments);
        }
    }
}
