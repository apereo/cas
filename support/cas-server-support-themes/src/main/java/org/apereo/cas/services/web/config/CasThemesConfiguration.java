package org.apereo.cas.services.web.config;

import com.google.common.collect.Sets;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.RegisteredServiceThemeBasedViewResolver;
import org.apereo.cas.services.web.ServiceThemeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.ViewResolver;
import org.thymeleaf.dialect.IPostProcessorDialect;
import org.thymeleaf.engine.AbstractTemplateHandler;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IText;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.postprocessor.PostProcessor;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

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
public class CasThemesConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ThymeleafProperties thymeleafProperties;

    @Autowired
    @Qualifier("thymeleafViewResolver")
    private ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    @Qualifier("argumentExtractors")
    private List argumentExtractors;

    @Autowired
    @Qualifier("serviceThemeResolverSupportedBrowsers")
    private Map serviceThemeResolverSupportedBrowsers;

    @Bean
    public ViewResolver registeredServiceViewResolver() {
        final RegisteredServiceThemeBasedViewResolver r = new RegisteredServiceThemeBasedViewResolver();

        r.setApplicationContext(this.thymeleafViewResolver.getApplicationContext());
        r.setCache(this.thymeleafProperties.isCache());
        if (!r.isCache()) {
            r.setCacheLimit(0);
        }
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
                return Sets.newHashSet(new PostProcessor(TemplateMode.parse(thymeleafProperties.getMode()),
                        CasThymeleafOutputTemplateHandler.class, Integer.MAX_VALUE));
            }

            @Override
            public String getName() {
                return CasThymeleafOutputTemplateHandler.class.getSimpleName();
            }
        });

        r.setTemplateEngine(engine);
        r.setViewNames(this.thymeleafViewResolver.getViewNames());
        r.setServicesManager(this.servicesManager);
        r.setArgumentExtractors(this.argumentExtractors);
        r.setPrefix(this.thymeleafProperties.getPrefix());
        r.setSuffix(this.thymeleafProperties.getSuffix());

        return r;
    }

    @Bean(name = {"serviceThemeResolver", "themeResolver"})
    public ThemeResolver serviceThemeResolver() {
        final ServiceThemeResolver resolver = new ServiceThemeResolver();
        resolver.setDefaultThemeName(casProperties.getTheme().getDefaultThemeName());
        resolver.setServicesManager(this.servicesManager);
        resolver.setMobileBrowsers(serviceThemeResolverSupportedBrowsers);
        return resolver;
    }

    /**
     * The Cas thymeleaf output template handler which attempts to compress the whitespace
     * produced by thymeleaf's conditional flags.
     */
    public static class CasThymeleafOutputTemplateHandler extends AbstractTemplateHandler {
        private boolean writeWhitespace;
        
        public CasThymeleafOutputTemplateHandler() {
        }

        @Override
        public void handleText(final IText text) {
            final String contentString = text.getText();
            if (!contentString.isEmpty() && contentString.trim().isEmpty()) {
                if (!writeWhitespace) {
                    return;
                }
                writeWhitespace = false;
            }
            super.handleText(text);
        }

        @Override
        public void handleCloseElement(final ICloseElementTag tag) {
            super.handleCloseElement(tag);
            writeWhitespace = true;
        }

        @Override
        public void handleOpenElement(final IOpenElementTag openElementTag) {
            super.handleOpenElement(openElementTag);
            writeWhitespace = true;
        }
    }
}
