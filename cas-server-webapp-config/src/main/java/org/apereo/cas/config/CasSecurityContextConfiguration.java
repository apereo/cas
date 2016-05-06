package org.apereo.cas.config;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.authorization.RequireAnyRoleAuthorizer;
import org.pac4j.core.authorization.generator.SpringSecurityPropertiesAuthorizationGenerator;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.client.direct.IpClient;
import org.pac4j.http.credentials.authenticator.IpRegexpAuthenticator;
import org.pac4j.springframework.web.RequiresAuthenticationInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMappingCustomizer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * This is {@link CasSecurityContextConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSecurityContextConfiguration")
public class CasSecurityContextConfiguration extends WebMvcConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasSecurityContextConfiguration.class);

    @Value("${cas.securityContext.adminpages.ip:127\\.0\\.0\\.1}")
    private String regexPattern;

    @Value("${cas.securityContext.adminpages.adminRoles:}")
    private String roles;

    @Value("${cas.securityContext.adminpages.loginUrl:}")
    private String loginUrl;

    @Value("${cas.securityContext.adminpages.service:}")
    private String callbackUrl;

    @Value("${cas.securityContext.adminpages.users:}")
    private Resource userPropertiesFile;

    /**
     * Web content interceptor web content interceptor.
     *
     * @return the web content interceptor
     */
    @RefreshScope
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
    @RefreshScope
    @Bean(name = "requiresAuthenticationStatusInterceptor")
    public RequiresAuthenticationInterceptor requiresAuthenticationStatusInterceptor() {
        return new RequiresAuthenticationInterceptor(new
                Config(new IpClient(new IpRegexpAuthenticator(this.regexPattern))), "IpClient");
    }

    /**
     * Config config.
     *
     * @return the config
     */
    @RefreshScope
    @Bean(name = "config")
    public Config config() {
        try {
            if (StringUtils.isNotBlank(this.loginUrl) && StringUtils.isNotBlank(this.callbackUrl)
                    && StringUtils.isNotBlank(this.roles)) {
                final IndirectClient client = new CasClient(this.loginUrl);
                final Properties properties = new Properties();
                properties.load(this.userPropertiesFile.getInputStream());
                client.setAuthorizationGenerator(new SpringSecurityPropertiesAuthorizationGenerator(properties));

                final Config cfg = new Config(this.callbackUrl, client);
                cfg.setAuthorizer(
                        new RequireAnyRoleAuthorizer(org.springframework.util.StringUtils.commaDelimitedListToSet(this.roles)));

                return cfg;
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return new Config();
    }

    /**
     * Requires authentication status admin endpoints interceptor requires authentication interceptor.
     *
     * @return the requires authentication interceptor
     */
    @RefreshScope
    @Bean(name = "requiresAuthenticationStatusAdminEndpointsInterceptor")
    public RequiresAuthenticationInterceptor requiresAuthenticationStatusAdminEndpointsInterceptor() {

        final Config cfg = config();
        if (cfg.getClients() == null) {
            return requiresAuthenticationStatusInterceptor();
        }

        final RequiresAuthenticationInterceptor interceptor = new RequiresAuthenticationInterceptor(cfg, "CasClient",
                "securityHeaders,csrfToken,RequireAnyRoleAuthorizer") {
            @Override
            protected void forbidden(final WebContext context, final List<Client> currentClients,
                                     final UserProfile profile) {
                context.setResponseStatus(HttpStatus.SC_FORBIDDEN);
            }
        };
        return interceptor;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(statusInterceptor()).addPathPatterns("/status/**");
        registry.addInterceptor(webContentInterceptor()).addPathPatterns("/*");
    }

    /**
     * Status interceptor handler interceptor adapter.
     *
     * @return the handler interceptor adapter
     */
    @Bean(name = "statusInterceptor")
    public HandlerInterceptorAdapter statusInterceptor() {
        return new HandlerInterceptorAdapter() {
            @Override
            public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                                     final Object handler) throws Exception {
                final String requestPath = request.getRequestURI();
                final Pattern pattern = Pattern.compile("/status(/)*$");

                if (pattern.matcher(requestPath).find()) {
                    return requiresAuthenticationStatusInterceptor().preHandle(request, response, handler);
                }
                return requiresAuthenticationStatusAdminEndpointsInterceptor().preHandle(request, response, handler);

            }
        };
    }

    /**
     * Mapping customizer endpoint handler mapping customizer.
     *
     * @return the endpoint handler mapping customizer
     */
    @RefreshScope
    @Bean(name = "mappingCustomizer")
    public EndpointHandlerMappingCustomizer mappingCustomizer() {
        return mapping -> mapping.setInterceptors(new Object[]{
                statusInterceptor()
        });
    }
}
