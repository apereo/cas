package org.apereo.cas.web.consent.config;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.pac4j.CasSecurityInterceptor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.consent.CasConsentOverviewController;
import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer;
import org.pac4j.core.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This is {@link CasConsentOverviewConfiguration}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@Configuration("casConsentOverviewConfiguration")
@ConditionalOnBean(name = "casSecurityContextConfiguration")
public class CasConsentOverviewConfiguration extends WebMvcConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasConsentOverviewConfiguration.class);

    private static final String CAS_CLIENT_NAME = "CasClient";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("config")
    private Config config;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Bean
    public CasConsentOverviewController casConsentOverviewController() {
        return new CasConsentOverviewController();
    }

    @Bean
    @RefreshScope
    public CasConsentOverviewSecurityInterceptor casConsentOverviewSecurityInterceptor() {
        final String authorizer = IsAuthenticatedAuthorizer.class.getSimpleName();
        if (!config.getAuthorizers().containsKey(authorizer)) {
            config.addAuthorizer(authorizer, new IsAuthenticatedAuthorizer());
        }
        return new CasConsentOverviewSecurityInterceptor(config, CAS_CLIENT_NAME,
                "securityHeaders,csrfToken,".concat(IsAuthenticatedAuthorizer.class.getSimpleName()));
    }

    /**
    * Initialize consent service.
    */
    @PostConstruct
    protected void registerConsentService() {
        final Service callbackService = this.webApplicationServiceFactory.createService(
                casProperties.getServer().getPrefix().concat("/consent.*"));
        if (!this.servicesManager.matchesExistingService(callbackService)) {
            LOGGER.debug("Initializing consent service [{}]", callbackService);

            final RegexRegisteredService service = new RegexRegisteredService();
            service.setId(Math.abs(RandomUtils.getInstanceNative().nextLong()));
            service.setEvaluationOrder(0);
            service.setName(service.getClass().getSimpleName());
            service.setDescription("CAS Consent Overview");
            service.setServiceId(callbackService.getId());

            LOGGER.debug("Saving consent service [{}] into the registry", service);
            this.servicesManager.save(service);
            this.servicesManager.load();
        }
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(casConsentOverviewSecurityInterceptor()).addPathPatterns("/consent**");
    }

    /**
    * The security interceptor for consent overview.
    */
    public static class CasConsentOverviewSecurityInterceptor extends CasSecurityInterceptor implements HandlerInterceptor {

        public CasConsentOverviewSecurityInterceptor(final Config config, final String clients,
                final String authorizers) {
            super(config, clients, authorizers);
        }

        @Override
        public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                               final Object handler, final ModelAndView modelAndView) throws Exception {
            if (modelAndView != null
                    && StringUtils.isNotBlank(request.getQueryString())
                    && request.getQueryString().contains(CasProtocolConstants.PARAMETER_TICKET)) {
                final RedirectView v = new RedirectView(request.getRequestURL().toString());
                v.setExposeModelAttributes(false);
                v.setExposePathVariables(false);
                modelAndView.setView(v);
            }
        }
    }
}
