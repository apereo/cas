package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.web.security.AdminPagesSecurityProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.web.pac4j.CasSecurityInterceptor;
import org.pac4j.cas.authorization.DefaultCasAuthorizationGenerator;
import org.pac4j.cas.client.direct.DirectCasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.authorization.generator.SpringSecurityPropertiesAuthorizationGenerator;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.IpClient;
import org.pac4j.http.credentials.authenticator.IpRegexpAuthenticator;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMappingCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSecurityContextConfiguration extends WebMvcConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasSecurityContextConfiguration.class);

    private static final String CAS_CLIENT_NAME = "CasClient";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public WebContentInterceptor webContentInterceptor() {
        final WebContentInterceptor interceptor = new WebContentInterceptor();
        interceptor.setCacheSeconds(0);
        interceptor.setAlwaysUseFullPath(true);
        return interceptor;
    }

    @RefreshScope
    @Bean
    public SecurityInterceptor requiresAuthenticationStatusInterceptor() {
        return new CasSecurityInterceptor(new
                Config(new IpClient(new IpRegexpAuthenticator(casProperties.getAdminPagesSecurity().getIp()))),
                "IpClient");
    }

    @RefreshScope
    @Bean
    public Config casAdminPagesPac4jConfig() {
        try {
            final AdminPagesSecurityProperties adminProps = casProperties.getAdminPagesSecurity();
            if (StringUtils.isNotBlank(adminProps.getLoginUrl())
                    && StringUtils.isNotBlank(adminProps.getService())) {

                final CasConfiguration casConfig = new CasConfiguration(adminProps.getLoginUrl());
                final DirectCasClient client = new DirectCasClient(casConfig);
                client.setName(CAS_CLIENT_NAME);
                final Config cfg = new Config(adminProps.getService(), client);
                if (adminProps.getUsers() == null) {
                    LOGGER.warn("List of authorized users for admin pages security is not defined. "
                            + "Allowing access for all authenticated users");
                    client.setAuthorizationGenerator(new DefaultCasAuthorizationGenerator<>());
                    cfg.setAuthorizer(new IsAuthenticatedAuthorizer());
                } else {
                    final Resource file = ResourceUtils.prepareClasspathResourceIfNeeded(adminProps.getUsers());
                    if (file != null && file.exists()) {
                        LOGGER.debug("Loading list of authorized users from [{}]", file);
                        final Properties properties = new Properties();
                        properties.load(file.getInputStream());
                        client.setAuthorizationGenerator(new SpringSecurityPropertiesAuthorizationGenerator(properties));
                        cfg.setAuthorizer(new RequireAnyRoleAuthorizer(adminProps.getAdminRoles()));
                    }
                }
                return cfg;
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return new Config();
    }

    @RefreshScope
    @Bean
    public SecurityInterceptor requiresAuthenticationStatusAdminEndpointsInterceptor() {
        final Config cfg = casAdminPagesPac4jConfig();
        if (cfg.getClients() == null) {
            return requiresAuthenticationStatusInterceptor();
        }
        final CasSecurityInterceptor interceptor = new CasSecurityInterceptor(cfg,
                CAS_CLIENT_NAME, "securityHeaders,csrfToken,".concat(getAuthorizerName()));
        return interceptor;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(statusInterceptor()).addPathPatterns("/status/**");
        registry.addInterceptor(webContentInterceptor()).addPathPatterns("/*");
    }

    @Bean
    public HandlerInterceptorAdapter statusInterceptor() {
        return new CasAdminStatusInterceptor();
    }

    @RefreshScope
    @Bean
    public EndpointHandlerMappingCustomizer mappingCustomizer() {
        return mapping -> mapping.setInterceptors(new Object[]{statusInterceptor()});
    }

    private String getAuthorizerName() {
        if (this.casProperties.getAdminPagesSecurity().getUsers() == null) {
            return IsAuthenticatedAuthorizer.class.getSimpleName();
        }
        return RequireAnyRoleAuthorizer.class.getSimpleName();
    }

    /**
     * The Cas admin status interceptor.
     */
    public class CasAdminStatusInterceptor extends HandlerInterceptorAdapter {
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

        @Override
        public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                final Object handler, final ModelAndView modelAndView) throws Exception {
            final String requestPath = request.getRequestURI();
            final Pattern pattern = Pattern.compile("/status(/)*$");

            if (pattern.matcher(requestPath).find()) {
                requiresAuthenticationStatusInterceptor().postHandle(request, response, handler, modelAndView);
            }
            requiresAuthenticationStatusAdminEndpointsInterceptor().postHandle(request, response, handler, modelAndView);
        }
    }
}
