package org.apereo.cas.notifications.mail;

import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import groovy.text.GStringTemplateEngine;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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

    @Builder.Default
    private final Optional<Locale> locale = Optional.empty();

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
            if (ScriptingUtils.isExternalGroovyScript(properties.getText())) {
                val cacheMgr = ApplicationContextProvider.getScriptResourceCacheManager().get();
                val script = cacheMgr.resolveScriptableResource(properties.getText(), properties.getText());
                val args = CollectionUtils.wrap("parameters", this.parameters, "logger", LOGGER);
                locale.ifPresent(l -> args.put("locale", l));
                script.setBinding(args);
                return script.execute(args.values().toArray(), String.class);
            }

            val templateFile = determineEmailTemplateFile();
            LOGGER.debug("Using email template file at [{}]", templateFile);
            val contents = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);
            if (templateFile.getName().endsWith(".gtemplate")) {
                val engine = new GStringTemplateEngine();
                val templateParams = new LinkedHashMap<>(this.parameters);
                locale.ifPresent(l -> templateParams.put("locale", l));
                val template = engine.createTemplate(contents).make(templateParams);
                return template.toString();
            }

            return String.format(contents, parameters.values().toArray());
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
            return String.format(properties.getText(), parameters.values().toArray());
        }
    }

    /**
     * Determine email template file.
     *
     * @return the file
     */
    protected File determineEmailTemplateFile() {
        return locale.map(Unchecked.function(l -> {
            val originalFile = new File(properties.getText());
            val localizedName = String.format("%s_%s.%s", FilenameUtils.getBaseName(originalFile.getName()),
                l.getLanguage(), FilenameUtils.getExtension(originalFile.getName()));
            val localizedFile = new File(originalFile.getParentFile(), localizedName);
            LOGGER.debug("Checking for localized email template file at [{}]", localizedFile.getPath());
            if (ResourceUtils.doesResourceExist(localizedFile.getPath())) {
                val templateResource = ResourceUtils.getRawResourceFrom(localizedFile.getPath());
                return templateResource.getFile();
            }
            return getDefaultEmailTemplateFile();
        })).orElseGet(Unchecked.supplier(this::getDefaultEmailTemplateFile));
    }

    private File getDefaultEmailTemplateFile() throws IOException {
        val templateResource = ResourceUtils.getResourceFrom(properties.getText());
        return templateResource.getFile();
    }
}
