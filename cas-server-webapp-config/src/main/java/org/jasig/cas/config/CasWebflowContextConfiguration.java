package org.jasig.cas.config;

import com.google.common.collect.ImmutableList;
import org.jasig.cas.web.flow.LogoutConversionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.binding.convert.ConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.ResourceBundleViewResolver;
import org.springframework.webflow.expression.spel.WebFlowSpringELExpressionParser;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;

/**
 * This is {@link CasWebflowContextConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casWebflowContextConfiguration")
public class CasWebflowContextConfiguration {

    private static final int VIEW_RESOLVER_ORDER = 10000;

    @Value("${cas.themeResolver.pathprefix:/WEB-INF/view/jsp}/default/ui/")
    private String resolverPathPrefix;

    @Bean(name = "expressionParser")
    public WebFlowSpringELExpressionParser expressionParser() {
        final WebFlowSpringELExpressionParser parser = new WebFlowSpringELExpressionParser(
                new SpelExpressionParser(),
                logoutConversionService());
        return parser;
    }

    @Bean(name = "logoutConversionService")
    public ConversionService logoutConversionService() {
        return new LogoutConversionService();
    }

    @Bean(name = "internalViewResolver")
    public InternalResourceViewResolver internalViewResolver() {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        resolver.setPrefix(this.resolverPathPrefix);
        resolver.setSuffix(".jsp");
        resolver.setOrder(VIEW_RESOLVER_ORDER);
        return resolver;
    }

    @Bean(name = "viewResolver")
    public ResourceBundleViewResolver viewResolver() {
        final ResourceBundleViewResolver resolver = new ResourceBundleViewResolver();
        resolver.setOrder(0);
        resolver.setBasename("cas_views");
        return resolver;
    }

    @Bean(name = "viewFactoryCreator")
    public MvcViewFactoryCreator viewFactoryCreator() {
        final MvcViewFactoryCreator resolver = new MvcViewFactoryCreator();
        resolver.setViewResolvers(ImmutableList.of(viewResolver(), internalViewResolver()));
        return resolver;
    }
}

