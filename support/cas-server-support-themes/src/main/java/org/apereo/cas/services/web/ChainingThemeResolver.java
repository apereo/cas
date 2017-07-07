package org.apereo.cas.services.web;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ChainingThemeResolver extends AbstractThemeResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChainingThemeResolver.class);
    
    private final Set<ThemeResolver> chain = new LinkedHashSet<>();

    /**
     * Add resolver to the chain.
     *
     * @param r the resolver
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
            final String resolverTheme = r.resolveThemeName(httpServletRequest);
            if (!resolverTheme.equalsIgnoreCase(getDefaultThemeName())) {
                return resolverTheme;
            }
        }
        return getDefaultThemeName();
    }

    @Override
    public void setThemeName(final HttpServletRequest httpServletRequest,
                             final HttpServletResponse httpServletResponse,
                             final String s) {
        // nothing to do here
    }
}
