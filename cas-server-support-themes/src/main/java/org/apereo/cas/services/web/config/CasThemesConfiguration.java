package org.apereo.cas.services.web.config;

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
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Comment;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Macro;
import org.thymeleaf.dom.Node;
import org.thymeleaf.dom.Text;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.ITemplateModeHandler;
import org.thymeleaf.templatemode.TemplateModeHandler;
import org.thymeleaf.templateparser.xmlsax.XhtmlAndHtml5NonValidatingSAXTemplateParser;
import org.thymeleaf.templatewriter.AbstractGeneralTemplateWriter;
import org.thymeleaf.templatewriter.ITemplateWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private ThymeleafProperties properties;

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
        r.setCache(this.properties.isCache());
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

        final SpringTemplateEngine engine = this.thymeleafViewResolver.getTemplateEngine();
        if (!engine.isInitialized()) {
            final ITemplateWriter writer = new AbstractGeneralTemplateWriter() {
                @Override
                protected boolean shouldWriteXmlDeclaration() {
                    return false;
                }

                @Override
                protected boolean useXhtmlTagMinimizationRules() {
                    return true;
                }

                @Override
                protected void writeText(final Arguments arguments, final Writer writer, final Text text)
                        throws IOException {
                    final String contentString = text.getContent();
                    if (!contentString.isEmpty() && contentString.trim().isEmpty()) {
                        return;
                    }
                    super.writeText(arguments, writer, text);
                }

                @Override
                public void writeNode(final Arguments arguments, final Writer writer, final Node node)
                        throws IOException {
                    super.writeNode(arguments, writer, node);
                    if (node instanceof Element || node instanceof Comment || node instanceof Macro) {
                        writer.write("\n");
                    }
                }
            };

            final ITemplateModeHandler handler = new TemplateModeHandler("HTML5",
                    new XhtmlAndHtml5NonValidatingSAXTemplateParser(2), writer);
            engine.setTemplateModeHandlers(Collections.singleton(handler));
        }
        r.setTemplateEngine(engine);
        r.setViewNames(this.thymeleafViewResolver.getViewNames());
        r.setServicesManager(this.servicesManager);
        r.setArgumentExtractors(this.argumentExtractors);
        r.setPrefix(this.properties.getPrefix());
        r.setSuffix(this.properties.getSuffix());

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

}
