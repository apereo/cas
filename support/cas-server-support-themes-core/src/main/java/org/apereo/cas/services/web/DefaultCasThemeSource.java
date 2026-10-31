package org.apereo.cas.services.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.themes.ThemeProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.web.theme.ResourceBundleThemeSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;

/**
 * This is {@link DefaultCasThemeSource}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultCasThemeSource extends ResourceBundleThemeSource {
    private final CasConfigurationProperties casProperties;

    @NonNull
    @Override
    protected MessageSource createMessageSource(
        @NonNull final String basename) {
        if (Strings.CI.equals(basename, ThemeProperties.DEFAULT_THEME_NAME)) {
            return createDefaultThemeMessageSource();
        }
        return findThemeFile(basename)
            .map(Unchecked.function(path -> loadMessageSourceFromPath(path, createDefaultThemeMessageSource())))
            .map(MessageSource.class::cast)
            .orElseGet(() -> createExtendedMessageSource(basename));
    }

    protected MessageSource createExtendedMessageSource(final String basename) {
        val source = (HierarchicalMessageSource) super.createMessageSource(basename);
        source.setParentMessageSource(createDefaultThemeMessageSource());
        return source;
    }

    /**
     * Create the message source of the default theme: {@code cas-theme-custom} on top of
     * {@code cas-theme-default}, itself overridden by a default theme file found under the template prefixes.
     * Every other theme falls back to this source, so deployment customizations apply to all themes.
     *
     * @return the message source
     */
    protected MessageSource createDefaultThemeMessageSource() {
        val classpathDefaults = super.createMessageSource(ThemeProperties.DEFAULT_THEME_NAME);
        val defaults = findThemeFile(ThemeProperties.DEFAULT_THEME_NAME)
            .map(Unchecked.function(path -> loadMessageSourceFromPath(path, classpathDefaults)))
            .map(MessageSource.class::cast)
            .orElse(classpathDefaults);
        val custom = (HierarchicalMessageSource) super.createMessageSource(
            Strings.CI.replace(ThemeProperties.DEFAULT_THEME_NAME, "-default", "-custom"));
        custom.setParentMessageSource(defaults);
        return custom;
    }

    /**
     * Load theme properties from the given path. Values do not depend on the request locale:
     * they are registered as common messages, which apply to every locale.
     *
     * @param path   the path
     * @param parent the parent message source
     * @return the message source
     * @throws Exception the exception
     */
    protected StaticMessageSource loadMessageSourceFromPath(final String path, final MessageSource parent) throws Exception {
        val source = new StaticMessageSource();
        try (val is = ResourceUtils.getRawResourceFrom(path).getInputStream()) {
            val properties = new Properties();
            properties.load(is);
            LOGGER.trace("Loaded theme properties [{}] from [{}]", properties.keySet(), path);
            source.setCommonMessages(properties);
        }
        source.setParentMessageSource(parent);
        return source;
    }

    private Optional<String> findThemeFile(final String basename) {
        return casProperties.getView().getTemplatePrefixes()
            .stream()
            .map(prefix -> Strings.CI.appendIfMissing(prefix, "/").concat(basename).concat(".properties"))
            .filter(ResourceUtils::doesResourceExist)
            .findFirst();
    }
}
