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
        return casProperties.getView().getTemplatePrefixes()
            .stream()
            .map(prefix -> Strings.CI.appendIfMissing(prefix, "/").concat(basename).concat(".properties"))
            .filter(ResourceUtils::doesResourceExist)
            .findFirst()
            .map(Unchecked.function(this::loadMessageSourceFromPath))
            .map(MessageSource.class::cast)
            .orElseGet(Unchecked.supplier(() -> createExtendedMessageSource(basename)));
    }

    protected MessageSource createExtendedMessageSource(final String basename) {
        if (Strings.CI.equals(basename, ThemeProperties.DEFAULT_THEME_NAME)) {
            val customTheme = Strings.CI.replace(basename, "-default", "-custom");
            val source = (HierarchicalMessageSource) super.createMessageSource(customTheme);
            source.setParentMessageSource(super.createMessageSource(basename));
            return source;
        }
        val source = (HierarchicalMessageSource) super.createMessageSource(basename);
        val defaultSource = super.createMessageSource(ThemeProperties.DEFAULT_THEME_NAME);
        source.setParentMessageSource(defaultSource);
        return source;
    }

    protected StaticMessageSource loadMessageSourceFromPath(final String path) throws Exception {
        val source = new StaticMessageSource();
        if (ResourceUtils.doesResourceExist(path)) {
            try (val is = ResourceUtils.getRawResourceFrom(path).getInputStream()) {
                val properties = new Properties();
                properties.load(is);
                properties.forEach((key, value) ->
                    List.of(Locale.US, Locale.CANADA, Locale.ENGLISH).forEach(locale -> {
                        LOGGER.trace("Adding theme property [{}] with value [{}] from [{}] for locale [{}]", key, value, path, locale);
                        source.addMessage(key.toString(), locale, value.toString());
                    }));
            }
            val defaultSource = super.createMessageSource(ThemeProperties.DEFAULT_THEME_NAME);
            source.setParentMessageSource(defaultSource);
        }
        return source;
    }
}
