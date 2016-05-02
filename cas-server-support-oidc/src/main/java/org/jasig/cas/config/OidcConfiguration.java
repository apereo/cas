package org.jasig.cas.config;

import org.jasig.cas.OidcConstants;
import org.jasig.cas.support.oauth.AccessTokenResponseGenerator;
import org.jasig.cas.web.OidcAccessTokenResponseGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * This is {@link OidcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("oidcConfiguration")
public class OidcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    @Qualifier("oauthInterceptor")
    private HandlerInterceptor oauthInterceptor;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(oidcInterceptor())
                .addPathPatterns('/' + OidcConstants.BASE_OIDC_URL.concat("/").concat("*"));
    }

    /**
     * Access token response generator access token response generator.
     *
     * @return the access token response generator
     */
    @RefreshScope
    @Bean(name = "accessTokenResponseGenerator")
    public AccessTokenResponseGenerator accessTokenResponseGenerator() {
        return new OidcAccessTokenResponseGenerator();
    }

    /**
     * Oidc interceptor handler interceptor.
     *
     * @return the handler interceptor
     */
    @Bean(name = "oidcInterceptor")
    public HandlerInterceptor oidcInterceptor() {
        return this.oauthInterceptor;
    }
}
