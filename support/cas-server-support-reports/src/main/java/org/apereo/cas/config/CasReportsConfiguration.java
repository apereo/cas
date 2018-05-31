package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.report.RegisteredServicesEndpoint;
import org.apereo.cas.web.report.SpringWebflowEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;

/**
 * This this {@link CasReportsConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Configuration("casReportsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasReportsConfiguration {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("cas3ServiceSuccessView")
    private View cas3ServiceSuccessView;

    @Autowired
    @Qualifier("cas3ServiceJsonView")
    private View cas3ServiceJsonView;

    @Autowired
    @Qualifier("cas2ServiceSuccessView")
    private View cas2ServiceSuccessView;

    @Autowired
    @Qualifier("cas1ServiceSuccessView")
    private View cas1ServiceSuccessView;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("principalFactory")
    private PrincipalFactory principalFactory;

    @Bean
    @ConditionalOnEnabledEndpoint
    public SpringWebflowEndpoint springWebflowEndpoint() {
        return new SpringWebflowEndpoint(casProperties, applicationContext);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public RegisteredServicesEndpoint registeredServicesReportEndpoint() {
        return new RegisteredServicesEndpoint(casProperties, servicesManager);
    }

    /*
    @Bean
    @ConditionalOnEnabledEndpoint
    public StatisticsEndpoint statisticsReportEndpoint() {
        return new StatisticsEndpoint(centralAuthenticationService, casProperties.getHost().getName());
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public CasResolveAttributesReportEndpoint resolveAttributesReportEndpoint() {
        return new CasResolveAttributesReportEndpoint(personDirectoryPrincipalResolver);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public SingleSignOnSessionsEndpoint singleSignOnSessionsReportEndpoint() {
        return new SingleSignOnSessionsReportEndpoint(centralAuthenticationService);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public CasReleaseAttributesReportEndpoint releaseAttributesReportEndpoint() {
        return new CasReleaseAttributesReportEndpoint(servicesManager, authenticationSystemSupport,
            webApplicationServiceFactory,
            principalFactory,
            cas3ServiceSuccessView,
            cas3ServiceJsonView,
            cas2ServiceSuccessView,
            cas1ServiceSuccessView,
            casProperties.getView().getCas2().isV3ForwardCompatible());
    }
    */
}
