package org.jasig.cas.mgmt.config;

import com.google.common.collect.ImmutableList;
import org.apache.http.HttpStatus;
import org.jasig.cas.audit.spi.ServiceManagementResourceResolver;
import org.jasig.cas.mgmt.services.audit.Pac4jAuditablePrincipalResolver;
import org.jasig.inspektr.audit.AuditTrailManagementAspect;
import org.jasig.inspektr.audit.spi.AuditActionResolver;
import org.jasig.inspektr.audit.spi.AuditResourceResolver;
import org.jasig.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.jasig.inspektr.audit.spi.support.ObjectCreationAuditActionResolver;
import org.jasig.inspektr.audit.spi.support.ParametersAsStringResourceResolver;
import org.jasig.inspektr.audit.support.Slf4jLoggingAuditTrailManager;
import org.jasig.inspektr.common.spi.PrincipalResolver;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.authorization.AuthorizationGenerator;
import org.pac4j.core.authorization.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.springframework.web.RequiresAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link CasManagementWebAppConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casManagementWebAppConfiguration")
@Lazy(true)
public class CasManagementWebAppConfiguration extends WebMvcConfigurerAdapter {
    
    /**
     * The User properties file.
     */
    @Value("${user.details.file.location:classpath:user-details.properties}")
    private Resource userPropertiesFile;
    
    /**
     * The Authorization generator.
     */
    @Autowired
    @Qualifier("authorizationGenerator")
    private AuthorizationGenerator authorizationGenerator;

    /**
     * The Roles.
     */
    @Value("${cas-management.securityContext.serviceProperties.adminRoles}")
    private String roles;

    /**
     * The Login url.
     */
    @Value("${cas.securityContext.casProcessingFilterEntryPoint.loginUrl}")
    private String loginUrl;

    /**
     * The Callback url.
     */
    @Value("${cas-management.securityContext.serviceProperties.service}")
    private String callbackUrl;

    /**
     * The Principal resolver.
     */
    @Autowired
    @Qualifier("auditablePrincipalResolver")
    private PrincipalResolver principalResolver;

    /**
     * The Audit resource resolver map.
     */
    @javax.annotation.Resource(name = "auditResourceResolverMap")
    private Map auditResourceResolverMap;

    /**
     * The Audit action resolver map.
     */
    @javax.annotation.Resource(name = "auditActionResolverMap")
    private Map auditActionResolverMap;

    /**
     * The Base name.
     */
    @Value("${cas-management.viewResolver.basename:default_views}")
    private String baseName;

    /**
     * A character encoding filter.
     *
     * @return the character encoding filter
     */
    @Bean(name = "characterEncodingFilter")
    public CharacterEncodingFilter characterEncodingFilter() {
        return new CharacterEncodingFilter("UTF-8", true);
    }

    /**
     * Require any role authorizer require any role authorizer.
     *
     * @return the require any role authorizer
     */
    @Bean(name = "requireAnyRoleAuthorizer")
    public RequireAnyRoleAuthorizer requireAnyRoleAuthorizer() {
        return new RequireAnyRoleAuthorizer(StringUtils.commaDelimitedListToSet(this.roles));
    }

    /**
     * Cas client cas client.
     *
     * @return the cas client
     */
    @Bean(name = "casClient")
    public CasClient casClient() {
        final CasClient client = new CasClient(this.loginUrl);
        client.setAuthorizationGenerator(authorizationGenerator);
        return client;
    }

    /**
     * Config config.
     *
     * @return the config
     */
    @Bean(name = "config")
    public Config config() {
        final Config cfg = new Config(this.callbackUrl, casClient());
        cfg.setAuthorizer(requireAnyRoleAuthorizer());
        return cfg;
    }

    /**
     * Root controller controller.
     *
     * @return the controller
     */
    @Bean(name="rootController")
    protected Controller rootController() {
        return new ParameterizableViewController() {
            @Override
            protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
                    throws Exception {
                final String url = request.getContextPath() + "/manage.html";
                return new ModelAndView(new RedirectView(response.encodeURL(url)));
            }

        };
    }

    /**
     * Handler mapping c simple url handler mapping.
     *
     * @return the simple url handler mapping
     */
    @Bean(name = "handlerMappingC")
    public SimpleUrlHandlerMapping handlerMappingC() {
        final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(1);
        mapping.setAlwaysUseFullPath(true);
        mapping.setRootHandler(rootController());

        final Properties properties = new Properties();
        properties.put("/*.html", new UrlFilenameViewController());
        mapping.setMappings(properties);
        return mapping;
    }
    
    /**
     * Cas management security interceptor requires authentication interceptor.
     *
     * @return the requires authentication interceptor
     */
    @Bean(name = "casManagementSecurityInterceptor")
    public HandlerInterceptorAdapter casManagementSecurityInterceptor() {
        final RequiresAuthenticationInterceptor interceptor = new RequiresAuthenticationInterceptor(config(), "CasClient",
                "securityHeaders,csrfToken,RequireAnyRoleAuthorizer") {
            @Override
            protected void forbidden(final WebContext context, final List<Client> currentClients, final UserProfile profile) {
                context.setResponseStatus(HttpStatus.SC_MOVED_TEMPORARILY);
                context.setResponseHeader("location", "authorizationFailure");
            }
        };
        return interceptor;
    }
    
    /**
     * Save service resource resolver parameters as string resource resolver.
     *
     * @return the parameters as string resource resolver
     */
    @Bean(name = "saveServiceResourceResolver")
    public ParametersAsStringResourceResolver saveServiceResourceResolver() {
        return new ParametersAsStringResourceResolver();
    }

    /**
     * Delete service resource resolver service management resource resolver.
     *
     * @return the service management resource resolver
     */
    @Bean(name = "deleteServiceResourceResolver")
    public ServiceManagementResourceResolver deleteServiceResourceResolver() {
        return new ServiceManagementResourceResolver();
    }

    /**
     * Save service action resolver default audit action resolver.
     *
     * @return the default audit action resolver
     */
    @Bean(name = "saveServiceActionResolver")
    public DefaultAuditActionResolver saveServiceActionResolver() {
        return new DefaultAuditActionResolver("_SUCCESS", "_FAILED");
    }

    /**
     * Delete service action resolver object creation audit action resolver.
     *
     * @return the object creation audit action resolver
     */
    @Bean(name = "deleteServiceActionResolver")
    public ObjectCreationAuditActionResolver deleteServiceActionResolver() {
        return new ObjectCreationAuditActionResolver("_SUCCESS", "_FAILED");
    }

    /**
     * Auditable principal resolver pac 4 j auditable principal resolver.
     *
     * @return the pac 4 j auditable principal resolver
     */
    @Bean(name = "auditablePrincipalResolver")
    public Pac4jAuditablePrincipalResolver auditablePrincipalResolver() {
        return new Pac4jAuditablePrincipalResolver();
    }

    /**
     * Audit trail management aspect audit trail management aspect.
     *
     * @return the audit trail management aspect
     */
    @Bean(name = "auditTrailManagementAspect")
    public AuditTrailManagementAspect auditTrailManagementAspect() {
        return new AuditTrailManagementAspect("CAS_Management",
                this.principalResolver, ImmutableList.of(auditTrailManager()),
                auditActionResolverMap,
                auditResourceResolverMap);
    }

    /**
     * Audit trail management.
     *
     * @return the audit trail management
     */
    @Bean(name = "auditTrailManager")
    public AuditTrailManager auditTrailManager() {
        return new Slf4jLoggingAuditTrailManager();
    }


    /**
     * User properties properties.
     *
     * @return the properties
     */
    @Bean(name = "userProperties")
    public Properties userProperties() {
        try {
            final Properties properties = new Properties();
            properties.load(this.userPropertiesFile.getInputStream());
            return properties;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Locale resolver cookie locale resolver.
     *
     * @return the cookie locale resolver
     */
    @Bean(name = "localeResolver")
    public CookieLocaleResolver localeResolver() {
        final CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }
        
    /**
     * Audit resource resolver map map.
     *
     * @return the map
     */
    @Bean(name="auditResourceResolverMap")
    public Map auditResourceResolverMap() {
        final Map<String, AuditResourceResolver> map = new HashMap<>();
        map.put("DELETE_SERVICE_RESOURCE_RESOLVER", deleteServiceResourceResolver());
        map.put("SAVE_SERVICE_RESOURCE_RESOLVER", saveServiceResourceResolver());
        return map;
    }
    
    /**
     * Audit action resolver map map.
     *
     * @return the map
     */
    @Bean(name="auditActionResolverMap")
    public Map auditActionResolverMap() {
        final Map<String, AuditActionResolver> map = new HashMap<>();
        map.put("DELETE_SERVICE_ACTION_RESOLVER", deleteServiceActionResolver());
        map.put("SAVE_SERVICE_ACTION_RESOLVER", saveServiceActionResolver());
        return map;
    }
    
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(casManagementSecurityInterceptor())
                .addPathPatterns("/**").excludePathPatterns("/callback*", "/logout*", "/authorizationFailure");
    }

    /**
     * Simple controller handler adapter simple controller handler adapter.
     *
     * @return the simple controller handler adapter
     */
    @Bean
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }
    
    /**
     * Place holder configurer property sources placeholder configurer.
     *
     * @return the property sources placeholder configurer
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
