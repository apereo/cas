package org.apereo.cas.notifications.mail;

import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;

import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link EmailMessageBodyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@SuperBuilder
public class EmailMessageBodyBuilder {
    @NonNull
    private final EmailProperties properties;

    @Builder.Default
    private final List<Object> parameters = new ArrayList<>();

    /**
     * Add parameter.
     *
     * @param object the object
     * @return the email message body builder
     */
    public EmailMessageBodyBuilder addParameter(final Object object) {
        parameters.add(object);
        return this;
    }

    /**
     * Produce.
     *
     * @return the message body
     */
    public String produce() {
        if (StringUtils.isBlank(properties.getText())) {
            LOGGER.warn("No email body is defined");
            return StringUtils.EMPTY;
        }
        try {
            val templateFile = ResourceUtils.getResourceFrom(properties.getText());
            if (ScriptingUtils.isExternalGroovyScript(properties.getText())) {
                return ScriptingUtils.executeGroovyScript(templateFile, parameters.toArray(), String.class, true);
            }
            val contents = FileUtils.readFileToString(templateFile.getFile(), StandardCharsets.UTF_8);
            return String.format(contents, parameters.toArray());
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
            return String.format(properties.getText(), parameters.toArray());
        }
    }
}
