package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.sba.CasServerInstanceIdGenerator;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.codecentric.boot.admin.client.config.ClientProperties;
import de.codecentric.boot.admin.client.config.SpringBootAdminClientEnabledCondition;
import de.codecentric.boot.admin.client.registration.BlockingRegistrationClient;
import de.codecentric.boot.admin.client.registration.RegistrationClient;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.SpringBootAdminServerEnabledCondition;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import de.codecentric.boot.admin.server.utils.jackson.AdminServerModule;
import de.codecentric.boot.admin.server.web.client.InstanceWebClientCustomizer;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * This is {@link CasSpringBootAdminAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SpringBootAdmin)
@AutoConfiguration
@EnableAsync(proxyTargetClass = false)
public class CasSpringBootAdminAutoConfiguration {

    @Configuration(value = "SpringBootAdminClientConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties({CasConfigurationProperties.class, ClientProperties.class})
    @Conditional(SpringBootAdminClientEnabledCondition.class)
    static class SpringBootAdminClientConfiguration {
        @Bean
        public RegistrationClient registrationClient(
            final ObjectMapper objectMapper,
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT) final HttpClient httpClient,
            final ClientProperties client) {

            objectMapper.findAndRegisterModules()
                .registerModule(new AdminServerModule(new String[]{".*password$"}));
            var builder = new RestTemplateBuilder()
                .connectTimeout(client.getConnectTimeout())
                .readTimeout(client.getReadTimeout())
                .customizers(template -> {
                    val requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient.wrappedHttpClient());
                    template.setRequestFactory(requestFactory);
                });
            if (client.getUsername() != null && client.getPassword() != null) {
                builder = builder.basicAuthentication(client.getUsername(), client.getPassword());
            }

            val restTemplate = builder.build();
            return new BlockingRegistrationClient(restTemplate);
        }
    }

    @Configuration(value = "SpringBootAdminServerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties({CasConfigurationProperties.class, AdminServerProperties.class})
    @Conditional(SpringBootAdminServerEnabledCondition.class)
    static class SpringBootAdminServerConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "springBootAdminWebClientCustomizer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public InstanceWebClientCustomizer springBootAdminWebClientCustomizer(
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT) final HttpClient httpClient) throws Exception {
            val sslContext = SslContextBuilder
                .forClient()
                .trustManager(httpClient.httpClientFactory().getTrustManagers()[0])
                .build();
            return builder -> {
                val nettyHttpClient = reactor.netty.http.client.HttpClient.create()
                    .compress(true)
                    .secure(t -> t.sslContext(sslContext));
                val reactorClientHttpConnector = new ReactorClientHttpConnector(nettyHttpClient);
                builder.webClient(WebClient.builder().clientConnector(reactorClientHttpConnector));
            };
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "springBootAdminEndpointConfigurer")
        public CasWebSecurityConfigurer<HttpSecurity> springBootAdminEndpointConfigurer(final AdminServerProperties properties) {
            return new CasWebSecurityConfigurer<>() {
                @Override
                public int getOrder() {
                    return Ordered.LOWEST_PRECEDENCE;
                }

                @Override
                public CasWebSecurityConfigurer<HttpSecurity> finish(final HttpSecurity http) throws Exception {
                    val adminContextPath = Strings.CI.prependIfMissing(properties.getContextPath(), "/");
                    val successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
                    successHandler.setTargetUrlParameter("redirectTo");
                    successHandler.setDefaultTargetUrl(adminContextPath);
                    http.authorizeHttpRequests(customizer -> customizer
                            .requestMatchers(
                                PathPatternRequestMatcher.withDefaults().matcher(adminContextPath + "/assets/**"),
                                PathPatternRequestMatcher.withDefaults().matcher(adminContextPath + "/login")).permitAll()
                            .requestMatchers(
                                PathPatternRequestMatcher.withDefaults().matcher(adminContextPath + "/**")).authenticated()
                        )
                        .formLogin(customizer -> customizer.loginPage(adminContextPath + "/login").successHandler(successHandler))
                        .logout(customizer -> customizer.logoutUrl(adminContextPath + "/logout"));
                    return this;
                }
            };
        }

        @Bean
        @ConditionalOnMissingBean(name = "springBootAdminInstanceIdGenerator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public InstanceIdGenerator instanceIdGenerator(final CasConfigurationProperties casProperties) {
            return new CasServerInstanceIdGenerator(casProperties);
        }
    }
}
