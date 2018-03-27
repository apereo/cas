package org.apereo.cas.services.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.theme.AbstractThemeResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
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
    public ChainingThemeResolver addResolver(final ThemeResolver r) {
        chain.add(r);
        return this;
    }

    @Override
    public String resolveThemeName(final HttpServletRequest httpServletRequest) {
        final Iterator<ThemeResolver> it = chain.iterator();
        while (it.hasNext()) {
            final ThemeResolver r = it.next();
            LOGGER.debug("Attempting to resolve theme via [{}]", r.getClass().getSimpleName());
            final String resolverTheme = r.resolveThemeName(httpServletRequest);
            if (!resolverTheme.equalsIgnoreCase(getDefaultThemeName())) {
                LOGGER.debug("Resolved theme [{}]", resolverTheme);
                return resolverTheme;
            }
        }
        LOGGER.debug("No specific theme could be found. Using default theme [{}}", getDefaultThemeName());
        return getDefaultThemeName();
    }

    @Override
    public void setThemeName(final HttpServletRequest httpServletRequest,
                             final HttpServletResponse httpServletResponse,
                             final String s) {
        // nothing to do here
    }
}
