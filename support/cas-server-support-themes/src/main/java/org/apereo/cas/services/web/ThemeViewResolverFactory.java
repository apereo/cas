package org.apereo.cas.services.web;

import org.springframework.web.servlet.ViewResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ThemeViewResolverFactory {
    @Nullable
    ViewResolver create(@Nonnull String theme);
}
