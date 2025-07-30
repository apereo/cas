package org.apereo.cas.services.web;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.theme.AbstractThemeResolver;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link ChainingThemeResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class ChainingThemeResolver extends AbstractThemeResolver {

    private final Set<ThemeResolver> chain = new LinkedHashSet<>();

    /**
     * Add resolver to the chain.
     *
     * @param r the resolver
     * @return the chaining theme resolver
     */
    @CanIgnoreReturnValue
    public ChainingThemeResolver addResolver(final ThemeResolver r) {
        chain.add(r);
        return this;
    }

    @Nonnull
    @Override
    public String resolveThemeName(
        final HttpServletRequest httpServletRequest) {
        for (val themeResolver : chain) {
            LOGGER.trace("Attempting to resolve theme via [{}]", themeResolver.getClass().getSimpleName());
            val resolverTheme = themeResolver.resolveThemeName(httpServletRequest);
            if (!resolverTheme.equalsIgnoreCase(getDefaultThemeName())) {
                LOGGER.trace("Resolved theme [{}]", resolverTheme);
                return resolverTheme;
            }
        }
        LOGGER.trace("No specific theme could be found. Using default theme [{}]", getDefaultThemeName());
        return getDefaultThemeName();
    }
}
