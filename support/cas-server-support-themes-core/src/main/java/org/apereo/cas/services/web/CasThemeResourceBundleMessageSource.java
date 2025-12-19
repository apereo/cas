package org.apereo.cas.services.web;

import module java.base;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * This is {@link CasThemeResourceBundleMessageSource}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public class CasThemeResourceBundleMessageSource extends ResourceBundleMessageSource {
    @Override
    protected @NonNull ResourceBundle doGetBundle(final @NonNull String basename, final @NonNull Locale locale) {
        try {
            val bundle = ResourceBundle.getBundle(basename, locale, getBundleClassLoader());
            if (bundle != null && !bundle.keySet().isEmpty()) {
                return bundle;
            }
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return null;
    }
}
