package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.view.ChainingTemplateViewResolver;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.ServletContextAware;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.UrlTemplateResolver;

import javax.servlet.ServletContext;

/**
 * This is {@link CasCoreViewsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("casCoreWebViewsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreViewsConfiguration implements ServletContextAware {

    private ServletContext servletContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ThymeleafProperties thymeleafProperties;

    @Bean
    public AbstractTemplateResolver chainingTemplateViewResolver() {
        final ChainingTemplateViewResolver chain = new ChainingTemplateViewResolver();

        casProperties.getView().getTemplatePrefixes().forEach(Unchecked.consumer(prefix -> {
            final FileTemplateResolver file = new FileTemplateResolver();
            configureTemplateViewResolver(file);
            final String prefixPath = ResourceUtils.getFile(prefix).getCanonicalPath();
            file.setPrefix(StringUtils.appendIfMissing(prefixPath, "/"));
            chain.addResolver(file);
        }));

        final ServletContextTemplateResolver servlet = new ServletContextTemplateResolver(this.servletContext);
        configureTemplateViewResolver(servlet);
        chain.addResolver(servlet);

        final UrlTemplateResolver url = new UrlTemplateResolver();
        configureTemplateViewResolver(url);
        chain.addResolver(url);

        return chain;
    }

    private void configureTemplateViewResolver(final AbstractConfigurableTemplateResolver resolver) {
        resolver.setCacheable(thymeleafProperties.isCache());
        resolver.setCharacterEncoding(thymeleafProperties.getEncoding().name());
        resolver.setCheckExistence(thymeleafProperties.isCheckTemplateLocation());
        resolver.setForceTemplateMode(true);
        resolver.setOrder(0);
        resolver.setSuffix(".html");
        resolver.setTemplateMode(thymeleafProperties.getMode());
    }

    @Override
    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
