package org.apereo.cas.adaptors.duo.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.api.DuoApiAuthenticationHandler;
import org.apereo.cas.adaptors.duo.authn.api.DuoApiAuthenticationMetaDataPopulator;
import org.apereo.cas.adaptors.duo.authn.api.DuoApiAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.web.DuoAuthenticationHandler;
import org.apereo.cas.adaptors.duo.authn.web.DuoAuthenticationMetaDataPopulator;
import org.apereo.cas.adaptors.duo.authn.web.DuoAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.web.flow.DuoAuthenticationWebflowAction;
import org.apereo.cas.adaptors.duo.web.flow.DuoAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.duo.web.flow.DuoMultifactorTrustWebflowConfigurer;
import org.apereo.cas.adaptors.duo.web.flow.DuoMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.duo.web.flow.DuoNonWebAuthenticationAction;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.authentication.FirstMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * This is {@link DuoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("duoConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DuoConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired(required = false)
    @Qualifier("multifactorAuthenticationProviderSelector")
    private MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector =
            new FirstMultifactorAuthenticationProviderSelector();

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("authenticationMetadataPopulators")
    private List authenticationMetadataPopulators;

    @Bean
    public FlowDefinitionRegistry duoFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-duo/*-webflow.xml");
        return builder.build();
    }

    @Bean
    public AuthenticationHandler duoAuthenticationHandler() {
        final DuoAuthenticationHandler h = new DuoAuthenticationHandler();
        h.setDuoAuthenticationService(duoAuthenticationService());
        h.setPrincipalFactory(duoPrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }

    @Bean
    public PrincipalFactory duoPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator duoAuthenticationMetaDataPopulator() {
        final DuoAuthenticationMetaDataPopulator pop = new DuoAuthenticationMetaDataPopulator();
        pop.setAuthenticationContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        pop.setAuthenticationHandler(duoAuthenticationHandler());
        pop.setProvider(duoAuthenticationProvider());
        return pop;
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator duoApiAuthenticationMetaDataPopulator() {
        final DuoApiAuthenticationMetaDataPopulator pop = new DuoApiAuthenticationMetaDataPopulator();
        pop.setAuthenticationContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        pop.setAuthenticationHandler(duoApiAuthenticationHandler());
        pop.setProvider(duoAuthenticationProvider());
        return pop;
    }
    
    @Bean
    public AuthenticationHandler duoApiAuthenticationHandler() {
        final DuoApiAuthenticationHandler h = new DuoApiAuthenticationHandler();
        h.setDuoApiAuthenticationService(duoApiAuthenticationService());
        h.setPrincipalFactory(duoPrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }
    
    @Bean
    @RefreshScope
    public DuoApiAuthenticationService duoApiAuthenticationService() {
        final DuoApiAuthenticationService s = new DuoApiAuthenticationService();
        return s;
    }
    
    @Bean
    @RefreshScope
    public DuoAuthenticationService duoAuthenticationService() {
        final DuoAuthenticationService s = new DuoAuthenticationService();
        s.setHttpClient(this.httpClient);
        return s;
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass duoBypassEvaluator() {
        return new DefaultMultifactorAuthenticationProviderBypass(
                casProperties.getAuthn().getMfa().getDuo().getBypass()
        );
    }
    
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider duoAuthenticationProvider() {
        final DuoMultifactorAuthenticationProvider p = new DuoMultifactorAuthenticationProvider();
        p.setDuoAuthenticationService(duoAuthenticationService());
        p.setBypassEvaluator(duoBypassEvaluator());
        return p;
    }

    @Bean
    public Action duoNonWebAuthenticationAction() {
        final DuoNonWebAuthenticationAction a = new DuoNonWebAuthenticationAction();
        return a;
    }
        
    @Bean
    public Action duoAuthenticationWebflowAction() {
        final DuoAuthenticationWebflowAction a = new DuoAuthenticationWebflowAction();
        a.setDuoAuthenticationWebflowEventResolver(duoAuthenticationWebflowEventResolver());
        return a;
    }

    @Bean
    public CasWebflowEventResolver duoAuthenticationWebflowEventResolver() {
        final DuoAuthenticationWebflowEventResolver r = new DuoAuthenticationWebflowEventResolver();
        r.setAuthenticationSystemSupport(authenticationSystemSupport);
        r.setCentralAuthenticationService(centralAuthenticationService);
        r.setMultifactorAuthenticationProviderSelector(multifactorAuthenticationProviderSelector);
        r.setServicesManager(servicesManager);
        r.setTicketRegistrySupport(ticketRegistrySupport);
        r.setWarnCookieGenerator(warnCookieGenerator);
        return r;
    }

    @ConditionalOnMissingBean(name = "duoMultifactorWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer duoMultifactorWebflowConfigurer() {
        final DuoMultifactorWebflowConfigurer r = new DuoMultifactorWebflowConfigurer();
        r.setDuoFlowRegistry(duoFlowRegistry());
        r.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        r.setFlowBuilderServices(flowBuilderServices);
        return r;
    }

    @PostConstruct
    protected void initializeServletApplicationContext() {
        if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getDuo().getDuoApiHost())
                && StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getDuo().getDuoSecretKey())) {
            this.authenticationHandlersResolvers.put(duoAuthenticationHandler(), null);
            this.authenticationHandlersResolvers.put(duoApiAuthenticationHandler(), null);
            authenticationMetadataPopulators.add(0, duoAuthenticationMetaDataPopulator());
            authenticationMetadataPopulators.add(0, duoApiAuthenticationMetaDataPopulator());
        }
    }

    /**
     * The Duo multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @Configuration("duoMultifactorTrustConfiguration")
    public class DuoMultifactorTrustConfiguration {

        @ConditionalOnProperty(prefix = "cas.authn.mfa.duo", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = "duoMultifactorTrustWebflowConfigurer")
        @Bean
        public CasWebflowConfigurer duoMultifactorTrustWebflowConfigurer() {
            final DuoMultifactorTrustWebflowConfigurer r = new DuoMultifactorTrustWebflowConfigurer();
            r.setFlowDefinitionRegistry(duoFlowRegistry());
            r.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
            r.setFlowBuilderServices(flowBuilderServices);
            r.setEnableDeviceRegistration(casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled());
            return r;
        }
    }
}
