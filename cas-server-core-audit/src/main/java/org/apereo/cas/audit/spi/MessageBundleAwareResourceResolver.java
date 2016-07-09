package org.apereo.cas.audit.spi;

import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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
        final Set<String> resolvedMessages = new HashSet<>(resolved.length);
        Arrays.stream(resolved).forEach(key -> {
            String defaultKey = e.getClass().getSimpleName();
            defaultKey = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(defaultKey), "_").toUpperCase();
            final String msg = this.context.getMessage(key, null, defaultKey, locale);
            resolvedMessages.add(msg);
        });
        return resolvedMessages.toArray(new String[] {});
    }
}
