package org.apereo.cas.services.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.autoconfigure.template.TemplateLocation;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractCachingViewResolver;
import org.thymeleaf.spring4.view.AbstractThymeleafView;
import java.util.Locale;
import lombok.Setter;

/**
 * {@link ThemeViewResolver} is a theme resolver that checks for a UI view in the specific theme before utilizing the
 * base UI view.
 *
 * @author Daniel Frett
 * @since 5.2.0
 */
@Slf4j
@Setter
@AllArgsConstructor
public class ThemeViewResolver extends AbstractCachingViewResolver {

    private final ViewResolver delegate;

    private final ThymeleafProperties thymeleafProperties;

    private final CasConfigurationProperties casProperties;

    private final String theme;

    @Override
    protected View loadView(final String viewName, final Locale locale) throws Exception {
        final View view = delegate.resolveViewName(viewName, locale);
        if (view instanceof AbstractThymeleafView) {
            final AbstractThymeleafView thymeleafView = (AbstractThymeleafView) view;
            configureTemplateThemeDefaultLocation(thymeleafView);
        }
        return view;
    }

    private void configureTemplateThemeDefaultLocation(final AbstractThymeleafView thymeleafView) {
        final String baseTemplateName = thymeleafView.getTemplateName();
        final String templateName = theme + "/" + baseTemplateName;
        final String path = thymeleafProperties.getPrefix().concat(templateName).concat(thymeleafProperties.getSuffix());
        final TemplateLocation location = new TemplateLocation(path);
        if (location.exists(getApplicationContext())) {
            thymeleafView.setTemplateName(templateName);
        }
    }

    /**
     * {@link ThemeViewResolverFactory} that will create a ThemeViewResolver for the specified theme.
     */
    @Getter
    @Setter
    public static class Factory implements ThemeViewResolverFactory, ApplicationContextAware {

        private final ViewResolver delegate;

        private final ThymeleafProperties thymeleafProperties;

        private final CasConfigurationProperties casProperties;

        private ApplicationContext applicationContext;

        public Factory(final ViewResolver delegate, final ThymeleafProperties thymeleafProperties, final CasConfigurationProperties casProperties) {
            this.delegate = delegate;
            this.casProperties = casProperties;
            this.thymeleafProperties = thymeleafProperties;
        }

        @Override
        public ThemeViewResolver create(final String theme) {
            final ThemeViewResolver resolver = new ThemeViewResolver(delegate, thymeleafProperties, casProperties, theme);
            resolver.setApplicationContext(applicationContext);
            resolver.setCache(thymeleafProperties.isCache());
            return resolver;
        }
    }
}
