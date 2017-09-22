package org.apereo.cas.config;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apereo.cas.adaptors.rest.RestAuthenticationApi;
import org.apereo.cas.adaptors.rest.RestAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.rest.RestAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * This is {@link CasRestAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casRestAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasRestAuthenticationConfiguration implements AuthenticationEventExecutionPlanConfigurer {

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public RestTemplate restAuthenticationTemplate() {
        try {
            final URI casHost = new URI(casProperties.getServer().getName());
            final HttpHost host = new HttpHost(casHost.getHost(), casHost.getPort(), casHost.getScheme());
            final ClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactoryBasicAuth(host);
            return new RestTemplate(factory);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @ConditionalOnMissingBean(name = "restAuthenticationApi")
    @Bean
    @RefreshScope
    public RestAuthenticationApi restAuthenticationApi() {
        return new RestAuthenticationApi(restAuthenticationTemplate(), casProperties.getAuthn().getRest().getUri());
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler restAuthenticationHandler() {
        final RestAuthenticationProperties rest = casProperties.getAuthn().getRest();
        final RestAuthenticationHandler r = new RestAuthenticationHandler(rest.getName(), restAuthenticationApi());
        r.setPasswordEncoder(Beans.newPasswordEncoder(rest.getPasswordEncoder()));
        return r;
    }

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        if (StringUtils.isNotBlank(casProperties.getAuthn().getRest().getUri())) {
            plan.registerAuthenticationHandlerWithPrincipalResolver(restAuthenticationHandler(), personDirectoryPrincipalResolver);
        }
    }

    private static class HttpComponentsClientHttpRequestFactoryBasicAuth extends HttpComponentsClientHttpRequestFactory {

        private final HttpHost host;

        /**
         * Instantiates a new Http components client http request factory basic auth.
         *
         * @param host the host
         */
        HttpComponentsClientHttpRequestFactoryBasicAuth(final HttpHost host) {
            super();
            this.host = host;
        }

        @Override
        protected HttpContext createHttpContext(final HttpMethod httpMethod, final URI uri) {
            return createHttpContext();
        }

        private HttpContext createHttpContext() {
            final AuthCache authCache = new BasicAuthCache();
            final BasicScheme basicAuth = new BasicScheme();
            authCache.put(host, basicAuth);
            final BasicHttpContext localcontext = new BasicHttpContext();
            localcontext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
            return localcontext;
        }
    }
}
