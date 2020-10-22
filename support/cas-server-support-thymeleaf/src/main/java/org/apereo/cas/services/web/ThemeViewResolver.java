package org.apereo.cas.services.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.template.TemplateLocation;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractCachingViewResolver;
import org.thymeleaf.spring5.view.AbstractThymeleafView;

import java.util.Locale;

/**
 * {@link ThemeViewResolver} is a theme resolver that checks for a UI view in the specific theme before utilizing the
 * base UI view.
 *
 * @author Daniel Frett
 * @since 5.2.0
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class ThemeViewResolver extends AbstractCachingViewResolver {

    private final ViewResolver delegate;

    private final ThymeleafProperties thymeleafProperties;

    private final String theme;

    @Override
    protected View loadView(final String viewName, final Locale locale) throws Exception {
        LOGGER.trace("Attempting to resolve view [{}] via locale [{}]", viewName, locale);
        val applicationContext = obtainApplicationContext();
        val view = applicationContext.containsBean(viewName)
            ? applicationContext.getBean(viewName, View.class)
            : delegate.resolveViewName(viewName, locale);
        
        if (view instanceof AbstractThymeleafView) {
            val thymeleafView = (AbstractThymeleafView) view;
            configureTemplateThemeDefaultLocation(thymeleafView);
        }
        return view;
    }

    @Override
    protected Object getCacheKey(final String viewName, final Locale locale) {
        return String.format("%s#%s", theme, super.getCacheKey(viewName, locale));
    }

    private void configureTemplateThemeDefaultLocation(final AbstractThymeleafView thymeleafView) {
        val baseTemplateName = thymeleafView.getTemplateName();
        val templateName = theme + '/' + baseTemplateName;
        val path = thymeleafProperties.getPrefix().concat(templateName).concat(thymeleafProperties.getSuffix());
        LOGGER.trace("Attempting to locate theme location at [{}]", path);
        val location = new TemplateLocation(path);
        val applicationContext = getApplicationContext();
        if (applicationContext != null && location.exists(applicationContext)) {
            thymeleafView.setTemplateName(templateName);
        }
    }

    /**
     * {@link ThemeViewResolverFactory} that will
     * create a {@link ThemeViewResolver} for the specified theme.
     */
    @Getter
    @Setter
    @Slf4j
    @RequiredArgsConstructor
    public static class Factory implements ThemeViewResolverFactory, ApplicationContextAware {

        private final ViewResolver delegate;

        private final ThymeleafProperties thymeleafProperties;

        private ApplicationContext applicationContext;

        @Override
        public ThemeViewResolver create(final String theme) {
            LOGGER.trace("Creating theme view resolver based on theme [{}]", theme);
            val resolver = new ThemeViewResolver(delegate, thymeleafProperties, theme);
            resolver.setApplicationContext(applicationContext);
            resolver.setCache(thymeleafProperties.isCache());
            return resolver;
        }
    }
}
