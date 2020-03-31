package org.apereo.cas.configuration.model.support.sms;

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
 * This is {@link SmsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@Setter
@RequiresModule(name = "cas-server-core-util", automated = true)
@Accessors(chain = true)
public class SmsProperties implements Serializable {

    private static final long serialVersionUID = -3713886839517507306L;

    /**
     * The body of the SMS message.
     */
    @RequiredProperty
    private String text;

    /**
     * The from address for the message.
     */
    @RequiredProperty
    private String from;

    /**
     * Principal attribute name that indicates the destination phone number
     * for this SMS message. The attribute must already be resolved and available
     * to the CAS principal.
     */
    @RequiredProperty
    private String attributeName = "phone";

    /**
     * Format body.
     *
     * @param arguments the arguments
     * @return the string
     */
    public String getFormattedText(final Object... arguments) {
        if (StringUtils.isBlank(this.text)) {
            LOGGER.warn("No SMS text is defined");
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
