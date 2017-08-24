package org.apereo.cas.services.web;

import org.springframework.web.servlet.ViewResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This interface can be used to create a ViewResolver for a specified theme.
 *
 * @author Daniel Frett
 * @since 5.2.0
 */
public interface ThemeViewResolverFactory {
    /**
     * Create a new ViewResolver for the specified theme.
     *
     * @param theme The theme to create the ViewResolver for
     * @return The ViewResolver
     */
    @Nullable
    ViewResolver create(@Nonnull String theme);
}
