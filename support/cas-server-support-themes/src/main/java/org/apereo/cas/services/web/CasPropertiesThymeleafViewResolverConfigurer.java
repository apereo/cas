package org.apereo.cas.services.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

@RequiredArgsConstructor
@Getter
@Order(CasThymeleafViewResolverConfigurer.CAS_PROPERTIES_ORDER)
public class CasPropertiesThymeleafViewResolverConfigurer implements CasThymeleafViewResolverConfigurer {

    private final CasConfigurationProperties casProperties;

    @Override
    public void configureThymeleafViewResolver(ThymeleafViewResolver thymeleafViewResolver) {
        thymeleafViewResolver.addStaticVariable("cas", casProperties);
        thymeleafViewResolver.addStaticVariable("casProperties", casProperties);
    }
}
