package org.apereo.cas.services.web;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.theme.AbstractThemeResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        val it = chain.iterator();
        while (it.hasNext()) {
            val r = it.next();
            LOGGER.trace("Attempting to resolve theme via [{}]", r.getClass().getSimpleName());
            val resolverTheme = r.resolveThemeName(httpServletRequest);
            if (!resolverTheme.equalsIgnoreCase(getDefaultThemeName())) {
                LOGGER.trace("Resolved theme [{}]", resolverTheme);
                return resolverTheme;
            }
        }
        LOGGER.trace("No specific theme could be found. Using default theme [{}]", getDefaultThemeName());
        return getDefaultThemeName();
    }

    @Override
    public void setThemeName(final HttpServletRequest httpServletRequest,
                             final HttpServletResponse httpServletResponse,
                             final String s) {
    }
}
