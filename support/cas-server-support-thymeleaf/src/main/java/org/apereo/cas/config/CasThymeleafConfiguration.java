package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.web.CasThymeleafLoginFormDirector;
import org.apereo.cas.services.web.CasThymeleafOutputTemplateHandler;
import org.apereo.cas.services.web.CasThymeleafViewResolverConfigurer;
import org.apereo.cas.services.web.ThemeBasedViewResolver;
import org.apereo.cas.services.web.ThemeViewResolver;
import org.apereo.cas.services.web.ThemeViewResolverFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.CasProtocolViewFactory;
import org.apereo.cas.web.view.CasProtocolThymeleafViewFactory;
import org.apereo.cas.web.view.ChainingTemplateViewResolver;
import org.apereo.cas.web.view.RestfulUrlTemplateResolver;
import org.apereo.cas.web.view.ThemeFileTemplateResolver;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.OrderComparator;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.ViewResolver;
import org.thymeleaf.dialect.IPostProcessorDialect;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.postprocessor.PostProcessor;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.util.List;
import java.util.Set;

/**
 * This is {@link CasThymeleafConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "casThymeleafConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(value = SpringTemplateEngine.class)
@ImportAutoConfiguration(ThymeleafAutoConfiguration.class)
public class CasThymeleafConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("themeResolver")
    private ObjectProvider<ThemeResolver> themeResolver;

    @Autowired
    @Qualifier("thymeleafViewResolver")
    private ObjectProvider<ThymeleafViewResolver> thymeleafViewResolver;

    @Autowired
    private ObjectProvider<List<CasThymeleafViewResolverConfigurer>> thymeleafViewResolverConfigurers;

    @Autowired
    private ObjectProvider<SpringTemplateEngine> springTemplateEngine;

    @Autowired
    private ObjectProvider<ThymeleafProperties> thymeleafProperties;

    @Bean
    @RefreshScope
    public AbstractTemplateResolver chainingTemplateViewResolver() {
        val chain = new ChainingTemplateViewResolver();

        val templatePrefixes = casProperties.getView().getTemplatePrefixes();
        templatePrefixes.forEach(Unchecked.consumer(prefix -> {
            val prefixPath = ResourceUtils.getFile(prefix).getCanonicalPath();
            val viewPath = StringUtils.appendIfMissing(prefixPath, "/");

            val rest = casProperties.getView().getRest();
            if (StringUtils.isNotBlank(rest.getUrl())) {
                val url = new RestfulUrlTemplateResolver(casProperties);
                configureTemplateViewResolver(url);
                chain.addResolver(url);
            }

            val theme = new ThemeFileTemplateResolver(casProperties);
            configureTemplateViewResolver(theme);
            theme.setPrefix(viewPath + "themes/%s/");
            chain.addResolver(theme);

            val file = new FileTemplateResolver();
            configureTemplateViewResolver(file);
            file.setPrefix(viewPath);
            chain.addResolver(file);
        }));

        chain.initialize();
        return chain;
    }

    @ConditionalOnMissingBean(name = "casPropertiesThymeleafViewResolverConfigurer")
    @Bean
    @RefreshScope
    public CasThymeleafViewResolverConfigurer casPropertiesThymeleafViewResolverConfigurer() {
        return new CasThymeleafViewResolverConfigurer() {
            @Override
            public int getOrder() {
                return 0;
            }

            @Override
            public void configureThymeleafViewResolver(final ThymeleafViewResolver thymeleafViewResolver) {
                thymeleafViewResolver.addStaticVariable("cas", casProperties);
                thymeleafViewResolver.addStaticVariable("casProperties", casProperties);
            }
        };
    }

    @ConditionalOnMissingBean(name = "registeredServiceViewResolver")
    @Bean
    @Autowired
    @RefreshScope
    public ViewResolver registeredServiceViewResolver(@Qualifier("themeViewResolverFactory") final ThemeViewResolverFactory themeViewResolverFactory) {
        val resolver = new ThemeBasedViewResolver(this.themeResolver.getObject(), themeViewResolverFactory);
        resolver.setOrder(thymeleafViewResolver.getObject().getOrder() - 1);
        return resolver;
    }

    @ConditionalOnMissingBean(name = "casThymeleafLoginFormDirector")
    @Bean
    @RefreshScope
    public CasThymeleafLoginFormDirector casThymeleafLoginFormDirector() {
        return new CasThymeleafLoginFormDirector();
    }

    @ConditionalOnMissingBean(name = "themeViewResolverFactory")
    @Bean
    @RefreshScope
    public ThemeViewResolverFactory themeViewResolverFactory() {
        val factory = new ThemeViewResolver.Factory(nonCachingThymeleafViewResolver(), thymeleafProperties.getObject());
        factory.setApplicationContext(applicationContext);
        return factory;
    }

    @ConditionalOnMissingBean(name = "casProtocolViewFactory")
    @Bean
    @RefreshScope
    public CasProtocolViewFactory casProtocolViewFactory() {
        return new CasProtocolThymeleafViewFactory(this.springTemplateEngine.getObject(), thymeleafProperties.getObject());
    }

    private ThymeleafViewResolver nonCachingThymeleafViewResolver() {
        val r = new ThymeleafViewResolver();

        val thymeleafResolver = this.thymeleafViewResolver.getObject();
        r.setAlwaysProcessRedirectAndForward(thymeleafResolver.getAlwaysProcessRedirectAndForward());
        r.setApplicationContext(thymeleafResolver.getApplicationContext());
        r.setCacheUnresolved(thymeleafResolver.isCacheUnresolved());
        r.setCharacterEncoding(thymeleafResolver.getCharacterEncoding());
        r.setContentType(thymeleafResolver.getContentType());
        r.setExcludedViewNames(thymeleafResolver.getExcludedViewNames());
        r.setOrder(thymeleafResolver.getOrder());
        r.setRedirectContextRelative(thymeleafResolver.isRedirectContextRelative());
        r.setRedirectHttp10Compatible(thymeleafResolver.isRedirectHttp10Compatible());
        r.setStaticVariables(thymeleafResolver.getStaticVariables());
        r.setForceContentType(thymeleafResolver.getForceContentType());

        val engine = SpringTemplateEngine.class.cast(thymeleafResolver.getTemplateEngine());
        if (!engine.isInitialized()) {
            engine.addDialect(new IPostProcessorDialect() {
                @Override
                public int getDialectPostProcessorPrecedence() {
                    return Integer.MAX_VALUE;
                }

                @Override
                public Set<IPostProcessor> getPostProcessors() {
                    return CollectionUtils.wrapSet(new PostProcessor(TemplateMode.parse(thymeleafProperties.getObject().getMode()),
                        CasThymeleafOutputTemplateHandler.class, Integer.MAX_VALUE));
                }

                @Override
                public String getName() {
                    return CasThymeleafOutputTemplateHandler.class.getSimpleName();
                }
            });
        }
        
        r.setTemplateEngine(engine);
        r.setViewNames(thymeleafResolver.getViewNames());
        r.setCache(false);

        thymeleafViewResolverConfigurers.getObject().stream()
            .sorted(OrderComparator.INSTANCE)
            .forEach(configurer -> configurer.configureThymeleafViewResolver(r));

        return r;
    }

    private void configureTemplateViewResolver(final AbstractConfigurableTemplateResolver resolver) {
        val props = thymeleafProperties.getObject();
        resolver.setCacheable(props.isCache());
        resolver.setCharacterEncoding(props.getEncoding().name());
        resolver.setCheckExistence(props.isCheckTemplateLocation());
        resolver.setForceTemplateMode(true);
        resolver.setOrder(0);
        resolver.setSuffix(".html");
        resolver.setTemplateMode(props.getMode());
    }
}
