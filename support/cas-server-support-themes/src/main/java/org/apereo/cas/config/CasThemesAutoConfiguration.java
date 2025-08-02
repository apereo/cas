package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.web.view.ViewProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.multitenancy.TenantThemeResolver;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.AggregateCasThemeSource;
import org.apereo.cas.services.web.ChainingThemeResolver;
import org.apereo.cas.services.web.DefaultCasThemeSource;
import org.apereo.cas.services.web.RegisteredServiceThemeResolver;
import org.apereo.cas.services.web.RequestHeaderThemeResolver;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.ui.context.ThemeSource;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ContentVersionStrategy;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.springframework.web.servlet.theme.CookieThemeResolver;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.theme.SessionThemeResolver;
import jakarta.annotation.Nonnull;

/**
 * This is {@link CasThemesAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties({CasConfigurationProperties.class, ThymeleafProperties.class, WebProperties.class})
@Slf4j
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Thymeleaf)
@AutoConfiguration
public class CasThemesAutoConfiguration {

    @ConditionalOnMissingBean(name = "casThemeSource")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ThemeSource themeSource(final CasConfigurationProperties casProperties) {
        if (casProperties.getView().getThemeSourceType() == ViewProperties.ThemeSourceTypes.AGGREGATE) {
            return new AggregateCasThemeSource(casProperties);
        }
        return new DefaultCasThemeSource(casProperties);
    }
    
    @ConditionalOnMissingBean(name = "casThemeResolver")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ThemeResolver themeResolver(
        final ObjectProvider<CasConfigurationProperties> casProperties,
        @Qualifier(TenantExtractor.BEAN_NAME)
        final ObjectProvider<TenantExtractor> tenantExtractor,
        @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
        final ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ObjectProvider<ServicesManager> servicesManager) {

        val defaultThemeName = casProperties.getObject().getTheme().getDefaultThemeName();
        val fixedResolver = new FixedThemeResolver();
        fixedResolver.setDefaultThemeName(defaultThemeName);

        val sessionThemeResolver = new SessionThemeResolver();
        sessionThemeResolver.setDefaultThemeName(defaultThemeName);

        val tgc = casProperties.getObject().getTgc();
        val cookieThemeResolver = new CookieThemeResolver(tgc);
        cookieThemeResolver.setDefaultThemeName(defaultThemeName);
        
        val serviceThemeResolver = new RegisteredServiceThemeResolver(servicesManager,
            authenticationRequestServiceSelectionStrategies, casProperties);
        serviceThemeResolver.setDefaultThemeName(defaultThemeName);

        val header = new RequestHeaderThemeResolver(casProperties.getObject().getTheme().getParamName());
        header.setDefaultThemeName(defaultThemeName);

        val chainingThemeResolver = new ChainingThemeResolver();
        chainingThemeResolver
            .addResolver(cookieThemeResolver)
            .addResolver(sessionThemeResolver)
            .addResolver(header)
            .addResolver(serviceThemeResolver)
            .addResolver(fixedResolver);

        if (casProperties.getObject().getMultitenancy().getCore().isEnabled()) {
            val tenantThemeResolver = new TenantThemeResolver(tenantExtractor, servicesManager,
                authenticationRequestServiceSelectionStrategies, casProperties);
            tenantThemeResolver.setDefaultThemeName(defaultThemeName);
            chainingThemeResolver.addResolver(tenantThemeResolver);
        }
        chainingThemeResolver.setDefaultThemeName(defaultThemeName);
        return chainingThemeResolver;
    }

    @Bean
    @ConditionalOnMissingBean(name = "themesStaticResourcesWebMvcConfigurer")
    public WebMvcConfigurer themesStaticResourcesWebMvcConfigurer(
        final CasConfigurationProperties casProperties,
        final WebProperties webProperties,
        final ThymeleafProperties thymeleafProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(
                @Nonnull
                final ResourceHandlerRegistry registry) {
                val templatePrefixes = casProperties.getView().getTemplatePrefixes();
                if (!templatePrefixes.isEmpty()) {
                    val registration = registry.addResourceHandler("/**");

                    val locations = templatePrefixes
                        .stream()
                        .map(prefix -> Strings.CI.appendIfMissing(prefix, "/"))
                        .toArray(String[]::new);
                    registration.addResourceLocations(locations);
                    registration.addResourceLocations(webProperties.getResources().getStaticLocations());

                    FunctionUtils.doIfNotNull(webProperties.getResources().getCache().getPeriod(), period -> registration.setCachePeriod((int) period.toSeconds()));
                    registration.setCacheControl(webProperties.getResources().getCache().getCachecontrol().toHttpCacheControl());
                    registration.setUseLastModified(true);
                    val cache = thymeleafProperties != null && thymeleafProperties.isCache();
                    val chainRegistration = registration.resourceChain(cache);
                    val resolver = new PathResourceResolver();

                    val resources = templatePrefixes
                        .stream()
                        .map(prefix -> Strings.CI.appendIfMissing(prefix, "/"))
                        .map(Unchecked.function(ResourceUtils::getRawResourceFrom))
                        .toArray(Resource[]::new);
                    LOGGER.debug("Adding resource handler for resources [{}]", (Object[]) resources);
                    resolver.setAllowedLocations(resources);
                    
                    val chainProperties = webProperties.getResources().getChain();
                    if (BooleanUtils.isTrue(chainProperties.getEnabled())) {
                        val paths = chainProperties.getStrategy().getContent().getPaths();
                        val versionResourceResolver = new VersionResourceResolver();
                        versionResourceResolver.addVersionStrategy(new ContentVersionStrategy(), paths);
                        chainRegistration.addResolver(versionResourceResolver);
                    }
                    chainRegistration.addResolver(resolver);
                }
            }
        };
    }
}
