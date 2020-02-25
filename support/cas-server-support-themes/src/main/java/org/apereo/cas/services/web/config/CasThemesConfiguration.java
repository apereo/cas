package org.apereo.cas.services.web.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.ChainingThemeResolver;
import org.apereo.cas.services.web.RegisteredServiceThemeResolver;
import org.apereo.cas.services.web.RequestHeaderThemeResolver;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.theme.CookieThemeResolver;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.theme.SessionThemeResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link CasThemesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casThemesConfiguration", proxyBeanMethods = true)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasThemesConfiguration {
    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    protected Map<String, String> serviceThemeResolverSupportedBrowsers() {
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
            authenticationRequestServiceSelectionStrategies.getObject(),
            new CasConfigurationProperties(),
            serviceThemeResolverSupportedBrowsers().entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> Pattern.compile(entry.getKey()), Map.Entry::getValue)));
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


}
