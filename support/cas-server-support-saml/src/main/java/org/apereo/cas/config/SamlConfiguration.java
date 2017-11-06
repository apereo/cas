package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceResponseBuilder;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.support.saml.web.SamlValidateController;
import org.apereo.cas.support.saml.web.view.Saml10FailureResponseView;
import org.apereo.cas.support.saml.web.view.Saml10SuccessResponseView;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.ValidationAuthorizer;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;

import java.nio.charset.StandardCharsets;
import java.util.Set;

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
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casAttributeEncoder")
    private ProtocolAttributeEncoder protocolAttributeEncoder;

    @Autowired
    @Qualifier("cas3ServiceJsonView")
    private View cas3ServiceJsonView;

    @Autowired
    @Qualifier("proxy20Handler")
    private ProxyHandler proxy20Handler;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean configBean;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("authenticationAttributeReleasePolicy")
    private AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    @Autowired
    @Qualifier("authenticationContextValidator")
    private AuthenticationContextValidator authenticationContextValidator;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
    private CasProtocolValidationSpecification cas20WithoutProxyProtocolValidationSpecification;

    @Autowired
    @Qualifier("defaultMultifactorTriggerSelectionStrategy")
    private MultifactorTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    @Autowired
    @Qualifier("serviceValidationAuthorizers")
    private Set<ValidationAuthorizer> validationAuthorizers;
            
    @ConditionalOnMissingBean(name = "casSamlServiceSuccessView")
    @RefreshScope
    @Bean
    public View casSamlServiceSuccessView() {
        return new Saml10SuccessResponseView(protocolAttributeEncoder,
                servicesManager, casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                saml10ObjectBuilder(), new DefaultArgumentExtractor(new SamlServiceFactory()),
                StandardCharsets.UTF_8.name(), casProperties.getSamlCore().getSkewAllowance(),
                casProperties.getSamlCore().getIssueLength(), casProperties.getSamlCore().getIssuer(),
                casProperties.getSamlCore().getAttributeNamespace(), authenticationAttributeReleasePolicy);
    }

    @ConditionalOnMissingBean(name = "casSamlServiceFailureView")
    @RefreshScope
    @Bean
    public View casSamlServiceFailureView() {
        return new Saml10FailureResponseView(protocolAttributeEncoder,
                servicesManager, casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                saml10ObjectBuilder(), new DefaultArgumentExtractor(new SamlServiceFactory()),
                StandardCharsets.UTF_8.name(), casProperties.getSamlCore().getSkewAllowance(),
                casProperties.getSamlCore().getIssueLength(), authenticationAttributeReleasePolicy);
    }


    @ConditionalOnMissingBean(name = "samlServiceResponseBuilder")
    @Bean
    public ResponseBuilder samlServiceResponseBuilder() {
        return new SamlServiceResponseBuilder();
    }

    @ConditionalOnMissingBean(name = "saml10ObjectBuilder")
    @Bean
    public Saml10ObjectBuilder saml10ObjectBuilder() {
        return new Saml10ObjectBuilder(this.configBean);
    }
    
    @Autowired
    @Bean
    public SamlValidateController samlValidateController(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor) {
        return new SamlValidateController(cas20WithoutProxyProtocolValidationSpecification,
                authenticationSystemSupport, servicesManager,
                centralAuthenticationService, proxy20Handler,
                argumentExtractor, multifactorTriggerSelectionStrategy,
                authenticationContextValidator, cas3ServiceJsonView,
                casSamlServiceSuccessView(), casSamlServiceFailureView(),
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(), 
                validationAuthorizers);
    }
}
