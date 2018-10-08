package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceResponseBuilder;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.support.saml.web.SamlValidateController;
import org.apereo.cas.support.saml.web.view.Saml10FailureResponseView;
import org.apereo.cas.support.saml.web.view.Saml10SuccessResponseView;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link SamlConfiguration} that creates the necessary OpenSAML context and beans.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlConfiguration {

    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casAttributeEncoder")
    private ObjectProvider<ProtocolAttributeEncoder> protocolAttributeEncoder;

    @Autowired
    @Qualifier("cas3ServiceJsonView")
    private ObjectProvider<View> cas3ServiceJsonView;

    @Autowired
    @Qualifier("proxy20Handler")
    private ObjectProvider<ProxyHandler> proxy20Handler;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean configBean;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("authenticationAttributeReleasePolicy")
    private ObjectProvider<AuthenticationAttributeReleasePolicy> authenticationAttributeReleasePolicy;

    @Autowired
    @Qualifier("authenticationContextValidator")
    private ObjectProvider<AuthenticationContextValidator> authenticationContextValidator;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
    private ObjectProvider<CasProtocolValidationSpecification> cas20WithoutProxyProtocolValidationSpecification;

    @Autowired
    @Qualifier("defaultMultifactorTriggerSelectionStrategy")
    private ObjectProvider<MultifactorTriggerSelectionStrategy> multifactorTriggerSelectionStrategy;

    @Autowired
    @Qualifier("serviceValidationAuthorizers")
    private ObjectProvider<ServiceTicketValidationAuthorizersExecutionPlan> validationAuthorizers;

    @ConditionalOnMissingBean(name = "casSamlServiceSuccessView")
    @RefreshScope
    @Bean
    public View casSamlServiceSuccessView() {
        val samlCore = casProperties.getSamlCore();
        return new Saml10SuccessResponseView(protocolAttributeEncoder.getIfAvailable(),
            servicesManager.getIfAvailable(),
            saml10ObjectBuilder(),
            argumentExtractor.getIfAvailable(),
            StandardCharsets.UTF_8.name(),
            samlCore.getSkewAllowance(),
            samlCore.getIssueLength(),
            samlCore.getIssuer(),
            samlCore.getAttributeNamespace(),
            authenticationAttributeReleasePolicy.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "casSamlServiceFailureView")
    @RefreshScope
    @Bean
    public View casSamlServiceFailureView() {
        return new Saml10FailureResponseView(protocolAttributeEncoder.getIfAvailable(),
            servicesManager.getIfAvailable(),
            saml10ObjectBuilder(),
            argumentExtractor.getIfAvailable(),
            StandardCharsets.UTF_8.name(),
            casProperties.getSamlCore().getSkewAllowance(),
            casProperties.getSamlCore().getIssueLength(),
            authenticationAttributeReleasePolicy.getIfAvailable());
    }


    @ConditionalOnMissingBean(name = "samlServiceResponseBuilder")
    @Bean
    public ResponseBuilder samlServiceResponseBuilder() {
        return new SamlServiceResponseBuilder(servicesManager.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "saml10ObjectBuilder")
    @Bean
    public Saml10ObjectBuilder saml10ObjectBuilder() {
        return new Saml10ObjectBuilder(this.configBean);
    }

    @Bean
    public SamlValidateController samlValidateController() {
        return new SamlValidateController(cas20WithoutProxyProtocolValidationSpecification.getIfAvailable(),
            authenticationSystemSupport.getIfAvailable(),
            servicesManager.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            proxy20Handler.getIfAvailable(),
            argumentExtractor.getIfAvailable(),
            multifactorTriggerSelectionStrategy.getIfAvailable(),
            authenticationContextValidator.getIfAvailable(),
            cas3ServiceJsonView.getIfAvailable(),
            casSamlServiceSuccessView(),
            casSamlServiceFailureView(),
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            validationAuthorizers.getIfAvailable(),
            casProperties.getSso().isRenewAuthnEnabled());
    }
}
