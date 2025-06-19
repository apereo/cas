package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.web.CasThymeleafOutputTemplateHandler;
import org.apereo.cas.services.web.CasThymeleafTemplatesDirector;
import org.apereo.cas.services.web.CasThymeleafViewResolverConfigurer;
import org.apereo.cas.services.web.ThemeBasedViewResolver;
import org.apereo.cas.services.web.ThemeViewResolver;
import org.apereo.cas.services.web.ThemeViewResolverFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.InetAddressUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.CasProtocolViewFactory;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.view.CasProtocolMustacheViewFactory;
import org.apereo.cas.web.view.CasProtocolThymeleafViewFactory;
import org.apereo.cas.web.view.CasThymeleafExpressionDialect;
import org.apereo.cas.web.view.ChainingTemplateViewResolver;
import org.apereo.cas.web.view.RestfulUrlTemplateResolver;
import org.apereo.cas.web.view.ThemeClassLoaderTemplateResolver;
import org.apereo.cas.web.view.ThemeFileTemplateResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.view.MustacheViewResolver;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.util.MimeType;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.ViewResolver;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.dialect.IPostProcessorDialect;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.postprocessor.PostProcessor;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.AbstractThymeleafView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This is {@link CasThymeleafAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(SpringTemplateEngine.class)
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
@ImportAutoConfiguration({
    MustacheAutoConfiguration.class,
    ThymeleafAutoConfiguration.class
})
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Thymeleaf)
@AutoConfiguration
public class CasThymeleafAutoConfiguration {

    private static final int THYMELEAF_VIEW_RESOLVER_ORDER = Ordered.LOWEST_PRECEDENCE - 5;

    @RequiredArgsConstructor
    private static final class CasPropertiesThymeleafViewResolverConfigurer
        implements CasThymeleafViewResolverConfigurer {
        private final CasConfigurationProperties casProperties;

        @Override
        public void configureThymeleafViewResolver(final ThymeleafViewResolver thymeleafViewResolver) {
            thymeleafViewResolver.addStaticVariable("cas", casProperties);
            thymeleafViewResolver.addStaticVariable("casProperties", casProperties);
            thymeleafViewResolver.addStaticVariable("host", InetAddressUtils.getCasServerHostName());
        }

        @Override
        public void configureThymeleafView(final AbstractThymeleafView thymeleafView) {
            thymeleafView.addStaticVariable("cas", casProperties);
            thymeleafView.addStaticVariable("casProperties", casProperties);
            thymeleafView.addStaticVariable("host", InetAddressUtils.getCasServerHostName());
        }

    }

    private static String appendCharset(final MimeType type, final String charset) {
        if (type.getCharset() != null) {
            return type.toString();
        }
        val parameters = new LinkedHashMap<String, String>();
        parameters.put("charset", charset);
        parameters.putAll(type.getParameters());
        return new MimeType(type, parameters).toString();
    }

    private static void configureTemplateViewResolver(final AbstractConfigurableTemplateResolver resolver,
                                                      final ThymeleafProperties thymeleafProperties) {
        resolver.setCacheable(thymeleafProperties.isCache());
        resolver.setCharacterEncoding(thymeleafProperties.getEncoding().name());
        resolver.setCheckExistence(thymeleafProperties.isCheckTemplateLocation());
        resolver.setForceTemplateMode(true);
        resolver.setOrder(0);
        resolver.setSuffix(".html");
        resolver.setTemplateMode(thymeleafProperties.getMode());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "chainingTemplateViewResolver")
    public AbstractTemplateResolver chainingTemplateViewResolver(
        @Qualifier("themeClassLoaderTemplateResolver") final ITemplateResolver themeClassLoaderTemplateResolver,
        @Qualifier("classLoaderTemplateResolver") final ITemplateResolver classLoaderTemplateResolver,
        final ThymeleafProperties thymeleafProperties,
        @Qualifier("themeResolver") final ThemeResolver themeResolver,
        final List<CasThymeleafViewResolverConfigurer> thymeleafViewResolverConfigurers,
        final CasConfigurationProperties casProperties) {

        val chain = new ChainingTemplateViewResolver();

        thymeleafViewResolverConfigurers
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(OrderComparator.INSTANCE)
            .map(CasThymeleafViewResolverConfigurer::registerTemplateResolver)
            .filter(Objects::nonNull)
            .filter(BeanSupplier::isNotProxy)
            .forEach(chain::addResolver);

        val rest = casProperties.getView().getRest();
        if (StringUtils.isNotBlank(rest.getUrl())) {
            val url = new RestfulUrlTemplateResolver(casProperties, themeResolver);
            configureTemplateViewResolver(url, thymeleafProperties);
            chain.addResolver(url);
        }

        val templatePrefixes = casProperties.getView().getTemplatePrefixes();
        templatePrefixes.forEach(prefix -> {
            try {
                val prefixPath = prefix.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)
                    ? prefix
                    : ResourceUtils.getFile(prefix).getCanonicalPath();
                val viewPath = StringUtils.appendIfMissing(prefixPath, "/");
                val theme = prefix.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)
                    ? new ThemeClassLoaderTemplateResolver(themeResolver)
                    : new ThemeFileTemplateResolver(casProperties, themeResolver);
                configureTemplateViewResolver(theme, thymeleafProperties);
                theme.setPrefix(StringUtils.removeStart(viewPath, ResourceUtils.CLASSPATH_URL_PREFIX) + "themes/%s/");
                chain.addResolver(theme);

                val template = prefix.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX) ? new ClassLoaderTemplateResolver() : new FileTemplateResolver();
                configureTemplateViewResolver(template, thymeleafProperties);
                template.setPrefix(StringUtils.removeStart(viewPath, ResourceUtils.CLASSPATH_URL_PREFIX));
                chain.addResolver(template);
            } catch (final Exception e) {
                LoggingUtils.warn(LOGGER,
                    String.format("Could not add template prefix '%s' to resolver: [%s]", prefix, e.getMessage()), e);
            }
        });

        chain.addResolver(themeClassLoaderTemplateResolver);
        chain.addResolver(classLoaderTemplateResolver);
        chain.initialize();
        return chain;
    }

    @Bean
    @ConditionalOnMissingBean(name = "themeClassLoaderTemplateResolver")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ITemplateResolver themeClassLoaderTemplateResolver(final ThymeleafProperties thymeleafProperties,
                                                              @Qualifier("themeResolver") final ThemeResolver themeResolver) {
        val themeCp = new ThemeClassLoaderTemplateResolver(themeResolver);
        configureTemplateViewResolver(themeCp, thymeleafProperties);
        themeCp.setPrefix("templates/%s/");
        return themeCp;
    }

    @Bean
    @ConditionalOnMissingBean(name = "classLoaderTemplateResolver")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ITemplateResolver classLoaderTemplateResolver(final ThymeleafProperties thymeleafProperties) {
        val cpResolver = new ClassLoaderTemplateResolver();
        configureTemplateViewResolver(cpResolver, thymeleafProperties);
        cpResolver.setPrefix("thymeleaf/templates/");
        return cpResolver;
    }

    @ConditionalOnMissingBean(name = "casPropertiesThymeleafViewResolverConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasThymeleafViewResolverConfigurer casPropertiesThymeleafViewResolverConfigurer(
        final CasConfigurationProperties casProperties) {
        return new CasPropertiesThymeleafViewResolverConfigurer(casProperties);
    }

    @Configuration(value = "ThymeleafWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class ThymeleafWebflowConfiguration {

        @ConditionalOnMissingBean(name = "casThymeleafTemplatesDirector")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasThymeleafTemplatesDirector casThymeleafTemplatesDirector(
            @Qualifier(CasWebflowExecutionPlan.BEAN_NAME) final CasWebflowExecutionPlan webflowExecutionPlan) {
            return new CasThymeleafTemplatesDirector(webflowExecutionPlan);
        }
    }

    @Configuration(value = "MustacheViewResolverConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class MustacheViewResolverConfiguration {

        @ConditionalOnMissingBean(name = CasProtocolViewFactory.BEAN_NAME_MUSTACHE_VIEW_FACTORY)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasProtocolViewFactory casProtocolMustacheViewFactory(final MustacheViewResolver mustacheViewResolver) {
            return new CasProtocolMustacheViewFactory(mustacheViewResolver);
        }
    }

    @Configuration(value = "ThymeleafViewResolverConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class ThymeleafViewResolverConfiguration {

        @ConditionalOnMissingBean(name = CasProtocolViewFactory.BEAN_NAME_THYMELEAF_VIEW_FACTORY)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasProtocolViewFactory casProtocolThymeleafViewFactory(
            @Qualifier("templateEngine") final SpringTemplateEngine springTemplateEngine,
            final ThymeleafProperties thymeleafProperties) {
            return new CasProtocolThymeleafViewFactory(springTemplateEngine, thymeleafProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SpringTemplateEngine templateEngine(final ThymeleafProperties thymeleafProperties,
                                                   final ObjectProvider<ITemplateResolver> templateResolvers,
                                                   final ObjectProvider<IDialect> dialects) {
            val engine = new SpringTemplateEngine();
            engine.setEnableSpringELCompiler(thymeleafProperties.isEnableSpringElCompiler());
            engine.setRenderHiddenMarkersBeforeCheckboxes(thymeleafProperties.isRenderHiddenMarkersBeforeCheckboxes());
            templateResolvers.orderedStream().forEach(engine::addTemplateResolver);
            dialects.orderedStream().forEach(engine::addDialect);
            return engine;
        }

        @ConditionalOnMissingBean(name = "registeredServiceViewResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ViewResolver registeredServiceViewResolver(
            @Qualifier("themeResolver") final ThemeResolver themeResolver,
            @Qualifier("themeViewResolverFactory") final ThemeViewResolverFactory themeViewResolverFactory) {
            val resolver = new ThemeBasedViewResolver(themeResolver, themeViewResolverFactory);
            resolver.setOrder(THYMELEAF_VIEW_RESOLVER_ORDER - 1);
            return resolver;
        }

        @ConditionalOnMissingBean(name = "themeViewResolverFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ThemeViewResolverFactory themeViewResolverFactory(
            final ThymeleafProperties thymeleafProperties,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("thymeleafViewResolver") final ThymeleafViewResolver thymeleafViewResolver,
            final List<CasThymeleafViewResolverConfigurer> thymeleafViewResolverConfigurers) {
            val factory = new ThemeViewResolver.Factory(thymeleafViewResolver, thymeleafProperties,
                casProperties, thymeleafViewResolverConfigurers);
            factory.setApplicationContext(applicationContext);
            return factory;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ThymeleafViewResolver thymeleafViewResolver(
            @Qualifier("templateEngine") final SpringTemplateEngine springTemplateEngine,
            final ThymeleafProperties thymeleafProperties,
            final ConfigurableApplicationContext applicationContext,
            final List<CasThymeleafViewResolverConfigurer> thymeleafViewResolverConfigurers) {
            val resolver = new ThymeleafViewResolver();
            resolver.setProducePartialOutputWhileProcessing(thymeleafProperties.getServlet().isProducePartialOutputWhileProcessing());
            resolver.setCharacterEncoding(thymeleafProperties.getEncoding().name());
            resolver.setApplicationContext(applicationContext);
            resolver.setExcludedViewNames(thymeleafProperties.getExcludedViewNames());
            resolver.setOrder(THYMELEAF_VIEW_RESOLVER_ORDER);
            resolver.setCache(false);
            resolver.setViewNames(thymeleafProperties.getViewNames());
            resolver.setContentType(appendCharset(thymeleafProperties.getServlet().getContentType(), resolver.getCharacterEncoding()));
            if (!springTemplateEngine.isInitialized()) {
                springTemplateEngine.addDialect(new IPostProcessorDialect() {

                    @Override
                    public int getDialectPostProcessorPrecedence() {
                        return Integer.MAX_VALUE;
                    }

                    @Override
                    public Set<IPostProcessor> getPostProcessors() {
                        return CollectionUtils.wrapSet(new PostProcessor(TemplateMode.parse(thymeleafProperties.getMode()), CasThymeleafOutputTemplateHandler.class, Integer.MAX_VALUE));
                    }

                    @Override
                    public String getName() {
                        return CasThymeleafOutputTemplateHandler.class.getSimpleName();
                    }
                });
            }
            resolver.setTemplateEngine(springTemplateEngine);
            thymeleafViewResolverConfigurers
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .sorted(OrderComparator.INSTANCE)
                .forEach(configurer -> configurer.configureThymeleafViewResolver(resolver));
            return resolver;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public IDialect casThymeleafExpressionDialect(
            @Qualifier("casThymeleafTemplatesDirector")
            final ObjectProvider<CasThymeleafTemplatesDirector> casThymeleafTemplatesDirector) {
            return new CasThymeleafExpressionDialect(casThymeleafTemplatesDirector);
        }
    }
}
