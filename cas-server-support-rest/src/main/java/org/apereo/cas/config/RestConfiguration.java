package org.apereo.cas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;

/**
 * This is {@link RestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casRestConfiguration")
public class RestConfiguration extends WebMvcConfigurerAdapter {

    @Resource(name="restAuthenticationThrottle")
    private HandlerInterceptor authenticationThrottle;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(this.authenticationThrottle).addPathPatterns("/v1/**");
    }
}
