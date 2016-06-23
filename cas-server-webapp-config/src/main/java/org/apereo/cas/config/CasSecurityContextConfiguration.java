package org.apereo.cas.config;

import com.github.lalyos.jfiglet.FigletFont;
import com.google.common.base.Throwables;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMappingCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("acceptUsersAuthenticationHandler")
    private AuthenticationHandler acceptUsersAuthenticationHandler;


    @Bean
    public WebContentInterceptor webContentInterceptor() {

        final WebContentInterceptor interceptor = new WebContentInterceptor();
        interceptor.setCacheSeconds(0);
        interceptor.setAlwaysUseFullPath(true);
        return interceptor;
    }

    @RefreshScope
    @Bean
    public RequiresAuthenticationInterceptor requiresAuthenticationStatusInterceptor() {
        return new RequiresAuthenticationInterceptor(new
                Config(new IpClient(new IpRegexpAuthenticator(casProperties.getAdminPagesSecurity().getIp()))),
                "IpClient");
    }

    @RefreshScope
    @Bean
    public Config config() {
        try {
            if (StringUtils.isNotBlank(casProperties.getAdminPagesSecurity().getLoginUrl())
                    && StringUtils.isNotBlank(casProperties.getAdminPagesSecurity().getService())
                    && StringUtils.isNotBlank(casProperties.getAdminPagesSecurity().getAdminRoles())) {

                final IndirectClient client = new CasClient(casProperties.getAdminPagesSecurity().getLoginUrl());
                final Properties properties = new Properties();
                properties.load(this.casProperties.getAdminPagesSecurity().getUsers().getInputStream());
                client.setAuthorizationGenerator(
                        new SpringSecurityPropertiesAuthorizationGenerator(properties));

                final Config cfg = new Config(casProperties.getAdminPagesSecurity().getService(), client);
                cfg.setAuthorizer(
                        new RequireAnyRoleAuthorizer(
                                org.springframework.util.StringUtils.commaDelimitedListToSet(
                                        casProperties.getAdminPagesSecurity().getAdminRoles())));

                return cfg;
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return new Config();
    }

    @RefreshScope
    @Bean
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

    @Bean
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

    @RefreshScope
    @Bean
    public EndpointHandlerMappingCustomizer mappingCustomizer() {
        return mapping -> mapping.setInterceptors(new Object[]{
                statusInterceptor()
        });
    }

    @PostConstruct
    public void init() {
        if (StringUtils.isNotBlank(casProperties.getAuthn().getAccept().getUsers())) {
            try {
                final String header = FigletFont.convertOneLine("!!STOP!!");
                LOGGER.warn('\n' + header
                        + "CAS is configured to accept a static list of credentials for authentication. "
                        + "While this is generally useful for demo purposes, it is STRONGLY recommended "
                        + "that you DISABLE this authentication method (by REMOVING the static list from "
                        + "your configuration) and switch to a mode that is more d for production such as "
                        + "LDAP/JDBC authentication. \n\n");
                this.authenticationHandlersResolvers.put(acceptUsersAuthenticationHandler, 
                        personDirectoryPrincipalResolver);
            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }
}
