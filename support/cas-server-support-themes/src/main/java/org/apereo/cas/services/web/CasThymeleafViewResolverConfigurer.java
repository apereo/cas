package org.apereo.cas.services.web;

import org.thymeleaf.spring4.view.ThymeleafViewResolver;

@FunctionalInterface
public interface CasThymeleafViewResolverConfigurer {

    int CAS_PROPERTIES_ORDER = 0;

    /**
     * Configures the CAS Thymeleaf View Resolver, eg to inject static variables.
     * @param thymeleafViewResolver The thymeleafViewResolver to configure
     */
    void configureThymeleafViewResolver(ThymeleafViewResolver thymeleafViewResolver);
}
