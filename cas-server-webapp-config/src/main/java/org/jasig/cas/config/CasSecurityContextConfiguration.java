package org.jasig.cas.config;

import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.IpClient;
import org.pac4j.http.credentials.authenticator.IpRegexpAuthenticator;
import org.pac4j.springframework.web.RequiresAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMappingCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

/**
 * This is {@link CasSecurityContextConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casSecurityContextConfiguration")
@Lazy(true)
public class CasSecurityContextConfiguration extends WebMvcConfigurerAdapter {
    

    /**
     * The Regex pattern.
     */
    @Value("${cas.securityContext.adminpages.ip:127\\.0\\.0\\.1}")
    private String regexPattern;

    /**
     * Web content interceptor web content interceptor.
     *
     * @return the web content interceptor
     */
    @Bean(name = "webContentInterceptor")
    public WebContentInterceptor webContentInterceptor() {
        final WebContentInterceptor interceptor = new WebContentInterceptor();
        interceptor.setCacheSeconds(0);
        interceptor.setAlwaysUseFullPath(true);
        return interceptor;
    }
    
    /**
     * Requires authentication interceptor requires authentication interceptor.
     *
     * @return the requires authentication interceptor
     */
    @Bean(name = "requiresAuthenticationStatusStatsInterceptor")
    public RequiresAuthenticationInterceptor requiresAuthenticationInterceptor() {
        return new RequiresAuthenticationInterceptor(new Config(new IpClient(new IpRegexpAuthenticator(this.regexPattern))), "IpClient");
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(requiresAuthenticationInterceptor())
                .addPathPatterns("/status/**");

        registry.addInterceptor(webContentInterceptor()).addPathPatterns("/*");
    }

    /**
     * Mapping customizer endpoint handler mapping customizer.
     *
     * @return the endpoint handler mapping customizer
     */
    @Bean
    public EndpointHandlerMappingCustomizer mappingCustomizer() {
        return mapping -> mapping.setInterceptors(new Object[] {requiresAuthenticationInterceptor()});
    }
}
