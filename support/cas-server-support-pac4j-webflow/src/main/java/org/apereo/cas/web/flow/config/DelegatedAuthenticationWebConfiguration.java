package org.apereo.cas.web.flow.config;

import org.apereo.cas.web.flow.DelegatedAuthenticationErrorViewResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration is separate from {@link DelegatedAuthenticationWebflowConfiguration} because
 * {@link ErrorViewResolver} @Beans are requested very early in the {@link org.springframework.boot.context.embedded.EmbeddedServletContainer}
 * startup process, so it contains 0 {@link org.springframework.beans.factory.annotation.Autowired} dependencies.
 *
 * @author sbearcsiro
 * @since 5.3.0
 */
@Configuration("delegatedAuthenticationWebConfiguration")
public class DelegatedAuthenticationWebConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "pac4jErrorViewResolver")
    public ErrorViewResolver pac4jErrorViewResolver() {
        return new DelegatedAuthenticationErrorViewResolver();
    }

}
