package org.apereo.cas.services.web;

import org.springframework.core.Ordered;
import org.thymeleaf.spring6.view.AbstractThymeleafView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * Spring beans that implement this interface will be used to configure the base
 * {@link ThymeleafViewResolver} used by the {@link ThemeViewResolverFactory}.
 *
 * @author sbearcsiro
 * @since 5.3.0
 */
public interface CasThymeleafViewResolverConfigurer extends Ordered {
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Configures the CAS Thymeleaf View Resolver, eg to inject static variables.
     *
     * @param thymeleafViewResolver The thymeleafViewResolver to configure
     */
    void configureThymeleafViewResolver(ThymeleafViewResolver thymeleafViewResolver);

    /**
     * Configure thymeleaf view.
     *
     * @param thymeleafView the thymeleaf view
     */
    void configureThymeleafView(AbstractThymeleafView thymeleafView);

    /**
     * Register template resolver.
     *
     * @return the template resolver
     */
    default ITemplateResolver registerTemplateResolver() {
        return null;
    }
}
