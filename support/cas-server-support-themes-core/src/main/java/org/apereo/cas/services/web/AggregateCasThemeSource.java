package org.apereo.cas.services.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.web.theme.ResourceBundleThemeSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;

/**
 * This is {@link AggregateCasThemeSource} which merges all the theme resource bundles that it can find.
 *
 * @author Hal Deadman
 * @since 6.6.8
 */
@RequiredArgsConstructor
@Slf4j
public class AggregateCasThemeSource extends ResourceBundleThemeSource {
    private final CasConfigurationProperties casProperties;

    @NonNull
    @Override
    protected MessageSource createMessageSource(
        @NonNull
        final String basename) {
        val source = new StaticMessageSource();
        source.setParentMessageSource(super.createMessageSource(basename));
        casProperties.getView().getTemplatePrefixes()
            .stream()
            .map(prefix -> Strings.CI.appendIfMissing(prefix, "/").concat(basename).concat(".properties"))
            .filter(ResourceUtils::doesResourceExist)
            .forEach(path -> {
                try (val is = ResourceUtils.getRawResourceFrom(path).getInputStream()) {
                    val properties = new Properties();
                    properties.load(is);
                    properties.forEach((key, value) -> {
                        LOGGER.trace("Loading theme property [{}] from [{}]", key, path);
                        source.addMessage(key.toString(), Locale.ENGLISH, value.toString());
                    });
                } catch (final IOException e) {
                    LOGGER.warn("Error loading resources from bundle: [{}] - [{}]", path, e.getMessage());
                }
            });
        return source;
    }
}
