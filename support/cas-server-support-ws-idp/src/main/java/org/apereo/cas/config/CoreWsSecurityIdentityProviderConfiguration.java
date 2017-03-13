package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.config.support.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.ws.idp.IdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.RealmAwareIdentityProvider;
import org.apereo.cas.ws.idp.authentication.WSFederationAuthenticationServiceSelectionStrategy;
import org.apereo.cas.ws.idp.impl.DefaultIdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.impl.DefaultRealmAwareIdentityProvider;
import org.apereo.cas.ws.idp.metadata.WSFederationMetadataServlet;
import org.apereo.cas.ws.idp.services.DefaultRelyingPartyTokenProducer;
import org.apereo.cas.ws.idp.services.WSFederationRelyingPartyTokenProducer;
import org.apereo.cas.ws.idp.web.WSFederationValidateRequestCallbackController;
import org.apereo.cas.ws.idp.web.WSWSFederationValidateRequestController;
import org.apereo.cas.ws.idp.web.flow.WSFederationMetadataUIAction;
import org.apereo.cas.ws.idp.web.flow.WSFederationWebflowConfigurer;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Lazy;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This is {@link CoreWsSecurityIdentityProviderConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("coreWsSecurityIdentityProviderConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportResource(locations = {"classpath:META-INF/cxf/cxf.xml"})
public class CoreWsSecurityIdentityProviderConfiguration implements AuthenticationServiceSelectionStrategyConfigurer {

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public WSWSFederationValidateRequestController federationValidateRequestController() {
        return new WSWSFederationValidateRequestController(idpConfigService(), servicesManager,
                webApplicationServiceFactory, casProperties, wsFederationAuthenticationServiceSelectionStrategy());
    }

    @Bean
    public WSFederationValidateRequestCallbackController federationValidateRequestCallbackController() {
        return new WSFederationValidateRequestCallbackController(idpConfigService(), servicesManager,
                webApplicationServiceFactory, casProperties, wsFederationRelyingPartyTokenProducer(),
                wsFederationAuthenticationServiceSelectionStrategy());
    }

    @Lazy
    @Bean
    public ServletRegistrationBean wsIdpMetadataServlet() {
        final WsFederationProperties wsfed = casProperties.getAuthn().getWsfedIdP();
        final ServletRegistrationBean bean = new ServletRegistrationBean();
        bean.setEnabled(true);
        bean.setName("federationServletIdentityProvider");
        bean.setServlet(new WSFederationMetadataServlet(wsfed.getIdp().getRealm()));
        bean.setUrlMappings(Collections.singleton("/ws/idp/metadata"));
        bean.setAsyncSupported(true);
        return bean;
    }

    @Bean
    public WSFederationRelyingPartyTokenProducer wsFederationRelyingPartyTokenProducer() {
        return new DefaultRelyingPartyTokenProducer(idpConfigService());
    }

    @Bean
    public IdentityProviderConfigurationService idpConfigService() {
        return new DefaultIdentityProviderConfigurationService(identityProviders());
    }

    @Bean
    public List<RealmAwareIdentityProvider> identityProviders() {
        try {
            final WsFederationProperties wsfed = casProperties.getAuthn().getWsfedIdP();
            final DefaultRealmAwareIdentityProvider idp = new DefaultRealmAwareIdentityProvider();
            idp.setRealm(wsfed.getIdp().getRealm());
            idp.setUri(wsfed.getIdp().getUri());
            idp.setStsUrl(new URL(casProperties.getServer().getPrefix().concat("/ws/sts/").concat(wsfed.getIdp().getUri())));
            idp.setIdpUrl(new URL(casProperties.getServer().getPrefix().concat("/ws/idp/federation")));
            idp.setCertificate(wsfed.getIdp().getCertificate());
            idp.setCertificatePassword(wsfed.getIdp().getCertificatePassword());
            idp.setSupportedProtocols(Arrays.asList("http://docs.oasis-open.org/wsfed/federation/200706", "http://docs.oasis-open.org/ws-sx/ws-trust/200512"));
            idp.setAuthenticationURIs(Collections.singletonMap("default", "federation"));
            idp.setDescription("WsFederation Identity Provider");
            idp.setDisplayName("WsFederation");
            return Arrays.asList(idp);
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    public AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy() {
        return new WSFederationAuthenticationServiceSelectionStrategy(webApplicationServiceFactory);
    }

    @Bean
    public Action wsFederationMetadataUIAction() {
        return new WSFederationMetadataUIAction(servicesManager, wsFederationAuthenticationServiceSelectionStrategy());
    }

    @Bean
    public CasWebflowConfigurer wsFederationWebflowConfigurer() {
        return new WSFederationWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, wsFederationMetadataUIAction());
    }

    @Override
    public void configureAuthenticationServiceSelectionStrategy(final AuthenticationServiceSelectionPlan plan) {
        plan.registerStrategy(wsFederationAuthenticationServiceSelectionStrategy());
    }
}
