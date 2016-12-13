package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.radius.JRadiusServerImpl;
import org.apereo.cas.adaptors.radius.RadiusAuthenticationMetaDataPopulator;
import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusProtocol;
import org.apereo.cas.adaptors.radius.authentication.RadiusMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.radius.authentication.RadiusTokenAuthenticationHandler;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowAction;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorTrustWebflowConfigurer;
import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.services.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is {@link RadiusMultifactorConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Nagai Takayuki
 * @since 5.0.0
 */
@Configuration("radiusMfaConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RadiusMultifactorConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers;

    @Autowired
    @Qualifier("authenticationMetadataPopulators")
    private List<AuthenticationMetaDataPopulator> authenticationMetadataPopulators;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

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
    @Qualifier("authenticationRequestServiceSelectionStrategies")
    private List<AuthenticationRequestServiceSelectionStrategy> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;


    @Bean
    public FlowDefinitionRegistry radiusFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder =
                new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-radius/*-webflow.xml");
        return builder.build();
    }

    @RefreshScope
    @Bean
    public List radiusTokenServers() {
        final List<JRadiusServerImpl> list = new ArrayList<>();
        final MultifactorAuthenticationProperties.Radius radius = casProperties.getAuthn().getMfa().getRadius();

        final RadiusClientFactory factory = new RadiusClientFactory();
        factory.setAccountingPort(radius.getClient().getAccountingPort());
        factory.setAuthenticationPort(radius.getClient().getAuthenticationPort());
        factory.setInetAddress(radius.getClient().getInetAddress());
        factory.setSharedSecret(radius.getClient().getSharedSecret());
        factory.setSocketTimeout(radius.getClient().getSocketTimeout());

        final RadiusProtocol protocol = RadiusProtocol.valueOf(radius.getServer().getProtocol());

        final JRadiusServerImpl impl = new JRadiusServerImpl(protocol, factory);
        impl.setRetries(radius.getServer().getRetries());
        impl.setNasIdentifier(radius.getServer().getNasIdentifier());
        impl.setNasPort(radius.getServer().getNasPort());
        impl.setNasPortId(radius.getServer().getNasPortId());
        impl.setNasRealPort(radius.getServer().getNasRealPort());
        impl.setNasIpAddress(radius.getServer().getNasIpAddress());
        impl.setNasIpv6Address(radius.getServer().getNasIpv6Address());

        list.add(impl);
        return list;
    }

    @RefreshScope
    @Bean
    public MultifactorAuthenticationProvider radiusAuthenticationProvider() {
        final RadiusMultifactorAuthenticationProvider p = new RadiusMultifactorAuthenticationProvider();
        p.setRadiusAuthenticationHandler(radiusTokenAuthenticationHandler());
        p.setBypassEvaluator(radiusBypassEvaluator());
        p.setGlobalFailureMode(casProperties.getAuthn().getMfa().getGlobalFailureMode());
        p.setOrder(casProperties.getAuthn().getMfa().getRadius().getRank());
        p.setId(casProperties.getAuthn().getMfa().getRadius().getId());
        return p;
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass radiusBypassEvaluator() {
        return new DefaultMultifactorAuthenticationProviderBypass(
                casProperties.getAuthn().getMfa().getRadius().getBypass(),
                ticketRegistrySupport
        );
    }

    @Bean
    @RefreshScope
    public RadiusAuthenticationMetaDataPopulator radiusAuthenticationMetaDataPopulator() {
        final RadiusAuthenticationMetaDataPopulator pop = new RadiusAuthenticationMetaDataPopulator();

        pop.setAuthenticationContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        pop.setAuthenticationHandler(radiusTokenAuthenticationHandler());
        pop.setProvider(radiusAuthenticationProvider());
        return pop;
    }

    @RefreshScope
    @Bean
    public RadiusTokenAuthenticationHandler radiusTokenAuthenticationHandler() {
        final RadiusTokenAuthenticationHandler a = new RadiusTokenAuthenticationHandler();
        final MultifactorAuthenticationProperties.Radius radius = casProperties.getAuthn().getMfa().getRadius();

        a.setPrincipalFactory(radiusTokenPrincipalFactory());
        a.setServicesManager(servicesManager);
        a.setServers(radiusTokenServers());
        a.setFailoverOnAuthenticationFailure(radius.isFailoverOnAuthenticationFailure());
        a.setFailoverOnException(radius.isFailoverOnException());
        a.setName(radius.getName());
        return a;
    }

    @Bean
    public PrincipalFactory radiusTokenPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }


    @Bean
    public Action radiusAuthenticationWebflowAction() {
        final RadiusAuthenticationWebflowAction w = new RadiusAuthenticationWebflowAction();
        w.setRadiusAuthenticationWebflowEventResolver(radiusAuthenticationWebflowEventResolver());
        return w;
    }

    @RefreshScope
    @Bean
    public CasWebflowEventResolver radiusAuthenticationWebflowEventResolver() {
        return new RadiusAuthenticationWebflowEventResolver(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport,
                warnCookieGenerator, authenticationRequestServiceSelectionStrategies, multifactorAuthenticationProviderSelector);
    }

    @ConditionalOnMissingBean(name = "radiusMultifactorWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer radiusMultifactorWebflowConfigurer() {
        final RadiusMultifactorWebflowConfigurer w = new RadiusMultifactorWebflowConfigurer();
        w.setRadiusFlowRegistry(radiusFlowRegistry());
        w.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        w.setFlowBuilderServices(flowBuilderServices);
        return w;
    }

    @PostConstruct
    protected void initializeRootApplicationContext() {
        authenticationHandlersResolvers.put(radiusTokenAuthenticationHandler(), null);
        authenticationMetadataPopulators.add(0, radiusAuthenticationMetaDataPopulator());
    }

    /**
     * The Radius multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.radius", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
    @Configuration("radiusMultifactorTrustConfiguration")
    public class RadiusMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "radiusMultifactorTrustConfiguration")
        @Bean
        public CasWebflowConfigurer radiusMultifactorTrustConfiguration() {
            final boolean deviceRegistrationEnabled = casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled();
            return new RadiusMultifactorTrustWebflowConfigurer(flowBuilderServices, deviceRegistrationEnabled, loginFlowDefinitionRegistry);
        }
    }
}
