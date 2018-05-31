package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.view.ChainingTemplateViewResolver;
import org.apereo.cas.web.view.RestfulUrlTemplateResolver;
import org.apereo.cas.web.view.ThemeFileTemplateResolver;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

/**
 * This is {@link CasCoreViewsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("casCoreWebViewsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreViewsConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ThymeleafProperties thymeleafProperties;

    @Bean
    public AbstractTemplateResolver chainingTemplateViewResolver() {
        final var chain = new ChainingTemplateViewResolver();

        final var templatePrefixes = casProperties.getView().getTemplatePrefixes();
        templatePrefixes.forEach(Unchecked.consumer(prefix -> {
            final var prefixPath = ResourceUtils.getFile(prefix).getCanonicalPath();
            final var viewPath = StringUtils.appendIfMissing(prefixPath, "/");

            final var rest = casProperties.getView().getRest();
            if (StringUtils.isNotBlank(rest.getUrl())) {
                final var url = new RestfulUrlTemplateResolver(casProperties);
                configureTemplateViewResolver(url);
                chain.addResolver(url);
            }

            final var theme = new ThemeFileTemplateResolver(casProperties);
            configureTemplateViewResolver(theme);
            theme.setPrefix(viewPath + "themes/%s/");
            chain.addResolver(theme);


            final var file = new FileTemplateResolver();
            configureTemplateViewResolver(file);
            file.setPrefix(viewPath);
            chain.addResolver(file);
        }));

        chain.initialize();
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
}
