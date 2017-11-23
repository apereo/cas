package org.apereo.cas.services.web.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.CasThymeleafOutputTemplateHandler;
import org.apereo.cas.services.web.ChainingThemeResolver;
import org.apereo.cas.services.web.RequestHeaderThemeResolver;
import org.apereo.cas.services.web.ServiceThemeResolver;
import org.apereo.cas.services.web.ThemeBasedViewResolver;
import org.apereo.cas.services.web.ThemeViewResolver;
import org.apereo.cas.services.web.ThemeViewResolverFactory;
import org.apereo.cas.util.CollectionUtils;
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
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.theme.CookieThemeResolver;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.theme.SessionThemeResolver;
import org.thymeleaf.dialect.IPostProcessorDialect;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.postprocessor.PostProcessor;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashMap;
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
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ThymeleafProperties thymeleafProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("thymeleafViewResolver")
    private ThymeleafViewResolver thymeleafViewResolver;

    @Bean
    public ViewResolver registeredServiceViewResolver() {
        final ThemeBasedViewResolver resolver = new ThemeBasedViewResolver(themeResolver(), themeViewResolverFactory());
        resolver.setOrder(thymeleafViewResolver.getOrder() - 1);
        return resolver;
    }

    @ConditionalOnMissingBean(name = "themeViewResolverFactory")
    @Bean
    public ThemeViewResolverFactory themeViewResolverFactory() {
        final ThemeViewResolver.Factory factory = new ThemeViewResolver.Factory(nonCachingThymeleafViewResolver(), thymeleafProperties);
        factory.setApplicationContext(applicationContext);
        return factory;
    }

    protected ThymeleafViewResolver nonCachingThymeleafViewResolver() {
        // clone existing ThymeleafViewResolver
        final ThymeleafViewResolver r = new ThymeleafViewResolver();

        r.setApplicationContext(this.thymeleafViewResolver.getApplicationContext());
        r.setCacheUnresolved(this.thymeleafViewResolver.isCacheUnresolved());
        r.setCharacterEncoding(this.thymeleafViewResolver.getCharacterEncoding());
        r.setContentType(this.thymeleafViewResolver.getContentType());
        r.setExcludedViewNames(this.thymeleafViewResolver.getExcludedViewNames());
        r.setOrder(this.thymeleafViewResolver.getOrder());
        r.setRedirectContextRelative(this.thymeleafViewResolver.isRedirectContextRelative());
        r.setRedirectHttp10Compatible(this.thymeleafViewResolver.isRedirectHttp10Compatible());
        r.setStaticVariables(this.thymeleafViewResolver.getStaticVariables());

        final SpringTemplateEngine engine = SpringTemplateEngine.class.cast(this.thymeleafViewResolver.getTemplateEngine());
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
        r.setViewNames(this.thymeleafViewResolver.getViewNames());

        // disable the cache
        r.setCache(false);

        // return this ViewResolver
        return r;
    }

    @Bean
    public Map serviceThemeResolverSupportedBrowsers() {
        final Map<String, String> map = new HashMap<>();
        map.put(".*iPhone.*", "iphone");
        map.put(".*Android.*", "android");
        map.put(".*Safari.*Pre.*", "safari");
        map.put(".*iPhone.*", "iphone");
        map.put(".*Nokia.*AppleWebKit.*", "nokiawebkit");
        return map;
    }

    @ConditionalOnMissingBean(name = "themeResolver")
    @Bean
    public ThemeResolver themeResolver() {
        final String defaultThemeName = casProperties.getTheme().getDefaultThemeName();

        final FixedThemeResolver fixedResolver = new FixedThemeResolver();
        fixedResolver.setDefaultThemeName(defaultThemeName);

        final SessionThemeResolver sessionThemeResolver = new SessionThemeResolver();
        sessionThemeResolver.setDefaultThemeName(defaultThemeName);

        final TicketGrantingCookieProperties tgc = casProperties.getTgc();
        final CookieThemeResolver cookieThemeResolver = new CookieThemeResolver();
        cookieThemeResolver.setDefaultThemeName(defaultThemeName);
        cookieThemeResolver.setCookieDomain(tgc.getDomain());
        cookieThemeResolver.setCookieHttpOnly(tgc.isHttpOnly());
        cookieThemeResolver.setCookieMaxAge(tgc.getMaxAge());
        cookieThemeResolver.setCookiePath(tgc.getPath());
        cookieThemeResolver.setCookieSecure(tgc.isSecure());

        final ServiceThemeResolver serviceThemeResolver = new ServiceThemeResolver(servicesManager,
                serviceThemeResolverSupportedBrowsers(), authenticationRequestServiceSelectionStrategies,
                this.resourceLoader);
        serviceThemeResolver.setDefaultThemeName(defaultThemeName);

        final RequestHeaderThemeResolver header = new RequestHeaderThemeResolver();
        header.setDefaultThemeName(defaultThemeName);

        final ChainingThemeResolver chainingThemeResolver = new ChainingThemeResolver();
        chainingThemeResolver.addResolver(cookieThemeResolver)
                .addResolver(sessionThemeResolver)
                .addResolver(header)
                .addResolver(serviceThemeResolver)
                .addResolver(fixedResolver);
        chainingThemeResolver.setDefaultThemeName(defaultThemeName);
        return chainingThemeResolver;
    }
}
