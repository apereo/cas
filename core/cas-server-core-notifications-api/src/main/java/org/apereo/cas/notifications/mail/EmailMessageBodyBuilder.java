package org.apereo.cas.notifications.mail;

import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jooq.lambda.Unchecked;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * This is {@link EmailMessageBodyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@SuperBuilder
public class EmailMessageBodyBuilder implements Supplier<String> {
    @NonNull
    private final EmailProperties properties;

    @Builder.Default
    @Getter
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
    @CanIgnoreReturnValue
    public EmailMessageBodyBuilder addParameter(final String key, final Object object) {
        parameters.put(key, object);
        return this;
    }

    @Override
    public String get() {
        if (StringUtils.isBlank(properties.getText())) {
            LOGGER.warn("No email body is defined");
            return StringUtils.EMPTY;
        }
        try {
            val scriptFactoryInstance = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();

            if (scriptFactoryInstance.isPresent()) {
                val scriptFactory = scriptFactoryInstance.get();
                if (scriptFactory.isScript(properties.getText())) {
                    val cacheMgr = ApplicationContextProvider.getScriptResourceCacheManager().orElseThrow();
                    val script = cacheMgr.resolveScriptableResource(properties.getText(), properties.getText());
                    val args = scriptFactory.isInlineScript(properties.getText())
                        ? new HashMap<>(this.parameters)
                        : CollectionUtils.<String, Object>wrap("parameters", this.parameters);
                    args.put("logger", LOGGER);
                    locale.ifPresent(loc -> args.put("locale", loc));
                    script.setBinding(args);
                    return script.execute(args.values().toArray(), String.class);
                }
            }

            val templateFile = determineEmailTemplateFile();
            LOGGER.debug("Using email template file at [{}]", templateFile);
            val contents = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);
            if (templateFile.getName().endsWith(".gtemplate") && scriptFactoryInstance.isPresent()) {
                val templateParams = new LinkedHashMap<>(this.parameters);
                locale.ifPresent(loc -> templateParams.put("locale", loc));
                return scriptFactoryInstance.get().createTemplate(contents, templateParams);
            }

            return formatEmailBody(contents);
        } catch (final Throwable e) {
            LOGGER.trace(e.getMessage(), e);
            return formatEmailBody(properties.getText());
        }
    }

    protected String formatEmailBody(final String contents) {
        val sub = new StringSubstitutor(this.parameters, "${", "}");
        return sub.replace(contents);
    }
    
    protected File determineEmailTemplateFile() {
        return locale.map(Unchecked.function(loc -> {
            val originalFile = new File(properties.getText());
            val localizedName = String.format("%s_%s.%s", FilenameUtils.getBaseName(originalFile.getName()),
                loc.getLanguage(), FilenameUtils.getExtension(originalFile.getName()));
            val localizedFile = new File(originalFile.getParentFile(), localizedName);
            LOGGER.trace("Checking for localized email template file at [{}]", localizedFile.getPath());
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
