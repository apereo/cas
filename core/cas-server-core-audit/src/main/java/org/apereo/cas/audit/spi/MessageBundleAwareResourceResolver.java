package org.apereo.cas.audit.spi;

import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link MessageBundleAwareResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MessageBundleAwareResourceResolver extends ReturnValueAsStringResourceResolver {

    @Autowired
    private ApplicationContext context;
    
    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception e) {
        final String[] resolved = super.resolveFrom(joinPoint, e);
        return resolveMessagesFromBundleOrDefault(resolved, e);
    }

    private String[] resolveMessagesFromBundleOrDefault(final String[] resolved, final Exception e) {
        final Locale locale = LocaleContextHolder.getLocale();
        final String defaultKey = Stream.of(StringUtils.splitByCharacterTypeCamelCase(e.getClass().getSimpleName()))
                .collect(Collectors.joining("_"))
                .toUpperCase();

        return Stream.of(resolved)
                .map(key -> this.context.getMessage(key, null, defaultKey, locale))
                .toArray(String[]::new);
    }
}
