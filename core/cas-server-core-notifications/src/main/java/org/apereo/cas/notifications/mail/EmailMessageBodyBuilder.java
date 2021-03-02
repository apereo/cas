package org.apereo.cas.notifications.mail;

import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private final Map<String, Object> parameters = new LinkedHashMap<>();

    /**
     * Add parameter.
     *
     * @param key    the key
     * @param object the object
     * @return the email message body builder
     */
    public EmailMessageBodyBuilder addParameter(final String key, final Object object) {
        parameters.put(key, object);
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
            val templateResource = ResourceUtils.getResourceFrom(properties.getText());
            val templateFile = templateResource.getFile();
            if (ScriptingUtils.isExternalGroovyScript(properties.getText())) {
                val cacheMgr = ApplicationContextProvider.getScriptResourceCacheManager().get();
                val script = cacheMgr.resolveScriptableResource(properties.getText(), templateFile.getName());
                val args = CollectionUtils.wrap("parameters", this.parameters, "logger", LOGGER);
                script.setBinding(args);
                return script.execute(args.values().toArray(), String.class);
            }
            val contents = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);
            return String.format(contents, parameters.values().toArray());
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
            return String.format(properties.getText(), parameters.values().toArray());
        }
    }
}
