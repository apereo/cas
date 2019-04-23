package org.apereo.cas.audit.spi.resource;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * This is {@link MessageBundleAwareResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class MessageBundleAwareResourceResolver extends ReturnValueAsStringResourceResolver {

    private final ApplicationContext context;

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception e) {
        val resolved = super.resolveFrom(joinPoint, e);
        return resolveMessagesFromBundleOrDefault(resolved, e);
    }

    private String[] resolveMessagesFromBundleOrDefault(final String[] resolved, final Exception e) {
        val locale = LocaleContextHolder.getLocale();
        val defaultKey = String.join("_", StringUtils.splitByCharacterTypeCamelCase(e.getClass().getSimpleName())).toUpperCase();
        return Stream.of(resolved)
            .map(key -> this.context.getMessage(key, null, defaultKey, locale))
            .filter(Objects::nonNull)
            .toArray(String[]::new);
    }
}
