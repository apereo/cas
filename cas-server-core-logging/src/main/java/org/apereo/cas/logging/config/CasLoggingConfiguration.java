package org.apereo.cas.logging.config;

import org.apereo.cas.logging.web.ThreadContextMDCServletFilter;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasLoggingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casLoggingConfiguration")
public class CasLoggingConfiguration {

    @Bean
    public FilterRegistrationBean threadContextMDCServletFilter() {
        final Map<String, String> initParams = new HashMap<>();
        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new ThreadContextMDCServletFilter());
        bean.setUrlPatterns(Collections.singleton("/*"));
        bean.setInitParameters(initParams);
        bean.setName("threadContextMDCServletFilter");
        bean.setOrder(0);
        return bean;

    }
}
