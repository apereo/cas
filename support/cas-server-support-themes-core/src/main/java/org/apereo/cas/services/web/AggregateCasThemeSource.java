package org.apereo.cas.services.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ResourceUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.ui.context.support.ResourceBundleThemeSource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

/**
 * This is {@link AggregateCasThemeSource}.
 *
 * @author Hal Deadman
 * @since 6.6.8
 */
@RequiredArgsConstructor
@Slf4j
public class AggregateCasThemeSource extends ResourceBundleThemeSource {
    private final CasConfigurationProperties casProperties;

    @Override
    protected MessageSource createMessageSource(
        @Nonnull
        final String basename) {
        val source = new StaticMessageSource();
        source.setParentMessageSource(super.createMessageSource(basename));
        casProperties.getView().getTemplatePrefixes()
            .stream()
            .map(prefix -> StringUtils.appendIfMissing(prefix, "/").concat(basename).concat(".properties"))
            .filter(ResourceUtils::doesResourceExist)
            .forEach(path -> {
                try (val is = ResourceUtils.getRawResourceFrom(path.toString()).getInputStream()) {
                    val properties = new Properties();
                    properties.load(is);
                    properties.forEach((key, value) -> {
                        LOGGER.trace("Loading theme property [{}] from [{}]", key, path);
                        source.addMessage(key.toString(), Locale.getDefault(), value.toString());
                    });
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            });
        return source;
    }
}
