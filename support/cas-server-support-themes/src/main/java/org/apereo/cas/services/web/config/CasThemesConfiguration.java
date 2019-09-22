package org.apereo.cas.services.web.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.CasThymeleafLoginFormDirector;
import org.apereo.cas.services.web.CasThymeleafOutputTemplateHandler;
import org.apereo.cas.services.web.CasThymeleafViewResolverConfigurer;
import org.apereo.cas.services.web.ChainingThemeResolver;
import org.apereo.cas.services.web.RegisteredServiceThemeResolver;
import org.apereo.cas.services.web.RequestHeaderThemeResolver;
import org.apereo.cas.services.web.ThemeBasedViewResolver;
import org.apereo.cas.services.web.ThemeViewResolver;
import org.apereo.cas.services.web.ThemeViewResolverFactory;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.OrderComparator;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.theme.CookieThemeResolver;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.theme.SessionThemeResolver;
import org.thymeleaf.dialect.IPostProcessorDialect;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.postprocessor.PostProcessor;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link CasThemesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casThemesConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Import(ThymeleafAutoConfiguration.class)
public class CasThemesConfiguration {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ThymeleafProperties thymeleafProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("thymeleafViewResolver")
    private ObjectProvider<ThymeleafViewResolver> thymeleafViewResolver;

    @Autowired
    private List<CasThymeleafViewResolverConfigurer> thymeleafViewResolverConfigurers;

    @ConditionalOnMissingBean(name = "casPropertiesThymeleafViewResolverConfigurer")
    @Bean
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
    public ViewResolver registeredServiceViewResolver() {
        val resolver = new ThemeBasedViewResolver(themeResolver(), themeViewResolverFactory());
        resolver.setOrder(thymeleafViewResolver.getObject().getOrder() - 1);
        return resolver;
    }

    @ConditionalOnMissingBean(name = "themeViewResolverFactory")
    @Bean
    public ThemeViewResolverFactory themeViewResolverFactory() {
        val factory = new ThemeViewResolver.Factory(nonCachingThymeleafViewResolver(), thymeleafProperties, casProperties);
        factory.setApplicationContext(applicationContext);
        return factory;
    }

    @ConditionalOnMissingBean(name = "casThymeleafLoginFormDirector")
    @Bean
    public CasThymeleafLoginFormDirector casThymeleafLoginFormDirector() {
        return new CasThymeleafLoginFormDirector();
    }

    @Bean
    public Map serviceThemeResolverSupportedBrowsers() {
        val map = new HashMap<String, String>();
        map.put(".*Android.*", "android");
        map.put(".*Safari.*Pre.*", "safari");
        map.put(".*iPhone.*", "iphone");
        map.put(".*Nokia.*AppleWebKit.*", "nokiawebkit");
        return map;
    }

    @ConditionalOnMissingBean(name = "themeResolver")
    @Bean
    public ThemeResolver themeResolver() {
        val defaultThemeName = casProperties.getTheme().getDefaultThemeName();

        val fixedResolver = new FixedThemeResolver();
        fixedResolver.setDefaultThemeName(defaultThemeName);

        val sessionThemeResolver = new SessionThemeResolver();
        sessionThemeResolver.setDefaultThemeName(defaultThemeName);

        val tgc = casProperties.getTgc();
        val cookieThemeResolver = new CookieThemeResolver();
        cookieThemeResolver.setDefaultThemeName(defaultThemeName);
        cookieThemeResolver.setCookieDomain(tgc.getDomain());
        cookieThemeResolver.setCookieHttpOnly(tgc.isHttpOnly());
        cookieThemeResolver.setCookieMaxAge(tgc.getMaxAge());
        cookieThemeResolver.setCookiePath(tgc.getPath());
        cookieThemeResolver.setCookieSecure(tgc.isSecure());

        val serviceThemeResolver = new RegisteredServiceThemeResolver(servicesManager.getObject(),
            serviceThemeResolverSupportedBrowsers(),
            authenticationRequestServiceSelectionStrategies.getObject(),
            this.resourceLoader,
            new CasConfigurationProperties());
        serviceThemeResolver.setDefaultThemeName(defaultThemeName);

        val header = new RequestHeaderThemeResolver(casProperties.getTheme().getParamName());
        header.setDefaultThemeName(defaultThemeName);

        val chainingThemeResolver = new ChainingThemeResolver();
        chainingThemeResolver.addResolver(cookieThemeResolver)
            .addResolver(sessionThemeResolver)
            .addResolver(header)
            .addResolver(serviceThemeResolver)
            .addResolver(fixedResolver);
        chainingThemeResolver.setDefaultThemeName(defaultThemeName);
        return chainingThemeResolver;
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
        engine.addDialect(new IPostProcessorDialect() {
            @Override
            public int getDialectPostProcessorPrecedence() {
                return Integer.MAX_VALUE;
            }

            @Override
            public Set<IPostProcessor> getPostProcessors() {
                return CollectionUtils.wrapSet(new PostProcessor(TemplateMode.parse(thymeleafProperties.getMode()),
                    CasThymeleafOutputTemplateHandler.class, Integer.MAX_VALUE));
            }

            @Override
            public String getName() {
                return CasThymeleafOutputTemplateHandler.class.getSimpleName();
            }
        });

        r.setTemplateEngine(engine);
        r.setViewNames(thymeleafResolver.getViewNames());
        r.setCache(false);

        thymeleafViewResolverConfigurers.stream()
            .sorted(OrderComparator.INSTANCE)
            .forEach(configurer -> configurer.configureThymeleafViewResolver(r));

        return r;
    }
}
