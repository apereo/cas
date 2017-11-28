package org.apereo.cas.services.web;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.template.TemplateLocation;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractCachingViewResolver;
import org.thymeleaf.spring4.view.AbstractThymeleafView;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * {@link ThemeViewResolver} is a theme resolver that checks for a UI view in the specific theme before utilizing the
 * base UI view.
 *
 * @author Daniel Frett
 * @since 5.2.0
 */
public class ThemeViewResolver extends AbstractCachingViewResolver {
    @Nonnull
    private final ViewResolver delegate;

    @Nonnull
    private final ThymeleafProperties thymeleafProperties;

    @Nonnull
    private final String theme;

    public ThemeViewResolver(@Nonnull final ViewResolver baseResolver,
                             @Nonnull final ThymeleafProperties thymeleafProperties, @Nonnull final String theme) {
        this.delegate = baseResolver;
        this.thymeleafProperties = thymeleafProperties;
        this.theme = theme;
    }

    @Override
    protected View loadView(final String viewName, final Locale locale) throws Exception {
        final View view = delegate.resolveViewName(viewName, locale);

        // support fallback for ThymeleafViews
        if (view instanceof AbstractThymeleafView) {
            final AbstractThymeleafView thymeleafView = (AbstractThymeleafView) view;
            final String baseTemplateName = thymeleafView.getTemplateName();

            // check for a template for this theme
            final String templateName = theme + "/" + baseTemplateName;
            final TemplateLocation location = new TemplateLocation(thymeleafProperties.getPrefix().concat(templateName)
                    .concat(thymeleafProperties.getSuffix()));
            if (location.exists(getApplicationContext())) {
                thymeleafView.setTemplateName(templateName);
            }
        }

        return view;
    }

    /**
     * {@link ThemeViewResolverFactory} that will create a ThemeViewResolver for the specified theme.
     */
    public static class Factory implements ThemeViewResolverFactory, ApplicationContextAware {
        @Nonnull
        private final ViewResolver delegate;
        @Nonnull
        private final ThymeleafProperties thymeleafProperties;

        private ApplicationContext applicationContext;

        public Factory(@Nonnull final ViewResolver delegate, @Nonnull final ThymeleafProperties thymeleafProperties) {
            this.delegate = delegate;
            this.thymeleafProperties = thymeleafProperties;
        }

        @Override
        public ThemeViewResolver create(@Nonnull final String theme) {
            final ThemeViewResolver resolver = new ThemeViewResolver(delegate, thymeleafProperties, theme);
            resolver.setApplicationContext(applicationContext);
            resolver.setCache(thymeleafProperties.isCache());
            return resolver;
        }

        @Override
        public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }
}
