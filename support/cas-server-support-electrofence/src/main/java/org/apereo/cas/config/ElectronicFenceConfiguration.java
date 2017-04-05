package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskContingencyPlan;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskMitigator;
import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.RiskBasedAuthenticationProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.impl.calcs.DateTimeAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.calcs.GeoLocationAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.calcs.IpAddressAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.calcs.UserAgentAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.engine.DefaultAuthenticationRiskEvaluator;
import org.apereo.cas.impl.engine.DefaultAuthenticationRiskMitigator;
import org.apereo.cas.impl.notify.AuthenticationRiskEmailNotifier;
import org.apereo.cas.impl.notify.AuthenticationRiskTwilioSmsNotifier;
import org.apereo.cas.impl.plans.BaseAuthenticationRiskContingencyPlan;
import org.apereo.cas.impl.plans.BlockAuthenticationContingencyPlan;
import org.apereo.cas.impl.plans.MultifactorAuthenticationContingencyPlan;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.RiskAwareAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.RiskAwareAuthenticationWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link ElectronicFenceConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("electronicFenceConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class ElectronicFenceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElectronicFenceConfiguration.class);

    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Autowired(required = false)
    private FlowBuilderServices flowBuilderServices;

    @Autowired(required = false)
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository casEventRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelector")
    private MultifactorAuthenticationProviderSelector selector;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @ConditionalOnMissingBean(name = "authenticationRiskEmailNotifier")
    @Bean
    @RefreshScope
    public AuthenticationRiskNotifier authenticationRiskEmailNotifier() {
        return new AuthenticationRiskEmailNotifier(communicationsManager);
    }

    @ConditionalOnMissingBean(name = "authenticationRiskSmsNotifier")
    @Bean
    @RefreshScope
    public AuthenticationRiskNotifier authenticationRiskSmsNotifier() {
        return new AuthenticationRiskTwilioSmsNotifier(communicationsManager);
    }

    @ConditionalOnMissingBean(name = "riskAwareAuthenticationWebflowEventResolver")
    @Bean
    @Autowired
    @RefreshScope
    public CasWebflowEventResolver riskAwareAuthenticationWebflowEventResolver(@Qualifier("defaultAuthenticationSystemSupport") 
                                                                                   final AuthenticationSystemSupport authenticationSystemSupport) {
        final CasWebflowEventResolver r = new RiskAwareAuthenticationWebflowEventResolver(authenticationSystemSupport, centralAuthenticationService,
                servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector, authenticationRiskEvaluator(),
                authenticationRiskMitigator(), casProperties);
        this.initialAuthenticationAttemptWebflowEventResolver.addDelegate(r, 0);
        return r;
    }

    @ConditionalOnMissingBean(name = "blockAuthenticationContingencyPlan")
    @Bean
    @RefreshScope
    public AuthenticationRiskContingencyPlan blockAuthenticationContingencyPlan() {
        final BlockAuthenticationContingencyPlan b = new BlockAuthenticationContingencyPlan();
        configureContingencyPlan(b);
        return b;
    }

    @ConditionalOnMissingBean(name = "multifactorAuthenticationContingencyPlan")
    @Bean
    @RefreshScope
    public AuthenticationRiskContingencyPlan multifactorAuthenticationContingencyPlan() {
        final MultifactorAuthenticationContingencyPlan b = new MultifactorAuthenticationContingencyPlan();
        configureContingencyPlan(b);
        return b;
    }

    @ConditionalOnMissingBean(name = "authenticationRiskMitigator")
    @Bean
    @RefreshScope
    public AuthenticationRiskMitigator authenticationRiskMitigator() {
        if (casProperties.getAuthn().getAdaptive().getRisk().getResponse().isBlockAttempt()) {
            return new DefaultAuthenticationRiskMitigator(blockAuthenticationContingencyPlan());
        }
        return new DefaultAuthenticationRiskMitigator(multifactorAuthenticationContingencyPlan());
    }

    @ConditionalOnMissingBean(name = "ipAddressAuthenticationRequestRiskCalculator")
    @Bean
    @RefreshScope
    public AuthenticationRequestRiskCalculator ipAddressAuthenticationRequestRiskCalculator() {
        return new IpAddressAuthenticationRequestRiskCalculator(this.casEventRepository);
    }

    @ConditionalOnMissingBean(name = "userAgentAuthenticationRequestRiskCalculator")
    @Bean
    @RefreshScope
    public AuthenticationRequestRiskCalculator userAgentAuthenticationRequestRiskCalculator() {
        return new UserAgentAuthenticationRequestRiskCalculator(this.casEventRepository);
    }

    @ConditionalOnMissingBean(name = "dateTimeAuthenticationRequestRiskCalculator")
    @Bean
    @RefreshScope
    public AuthenticationRequestRiskCalculator dateTimeAuthenticationRequestRiskCalculator() {
        return new DateTimeAuthenticationRequestRiskCalculator(this.casEventRepository,
                casProperties.getAuthn().getAdaptive().getRisk().getDateTime().getWindowInHours());
    }

    @ConditionalOnMissingBean(name = "geoLocationAuthenticationRequestRiskCalculator")
    @Bean
    @RefreshScope
    public AuthenticationRequestRiskCalculator geoLocationAuthenticationRequestRiskCalculator() {
        return new GeoLocationAuthenticationRequestRiskCalculator(this.casEventRepository);
    }

    @ConditionalOnMissingBean(name = "riskAwareAuthenticationWebflowConfigurer")
    @Bean
    @RefreshScope
    public CasWebflowConfigurer riskAwareAuthenticationWebflowConfigurer() {
        return new RiskAwareAuthenticationWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @ConditionalOnMissingBean(name = "authenticationRiskEvaluator")
    @Bean
    @RefreshScope
    public AuthenticationRiskEvaluator authenticationRiskEvaluator() {
        final RiskBasedAuthenticationProperties risk = casProperties.getAuthn().getAdaptive().getRisk();
        final Set<AuthenticationRequestRiskCalculator> calculators = new HashSet<>();

        if (risk.getIp().isEnabled()) {
            calculators.add(ipAddressAuthenticationRequestRiskCalculator());
        }
        if (risk.getAgent().isEnabled()) {
            calculators.add(userAgentAuthenticationRequestRiskCalculator());
        }
        if (risk.getDateTime().isEnabled()) {
            calculators.add(dateTimeAuthenticationRequestRiskCalculator());
        }
        if (risk.getGeoLocation().isEnabled()) {
            calculators.add(geoLocationAuthenticationRequestRiskCalculator());
        }

        if (calculators.isEmpty()) {
            LOGGER.warn("No risk calculators are defined to examine authentication requests");
        }

        return new DefaultAuthenticationRiskEvaluator(calculators);
    }

    private void configureContingencyPlan(final BaseAuthenticationRiskContingencyPlan b) {
        final RiskBasedAuthenticationProperties.Response.Mail mail =
                casProperties.getAuthn().getAdaptive().getRisk().getResponse().getMail();
        if (StringUtils.isNotBlank(mail.getText()) && StringUtils.isNotBlank(mail.getFrom()) && StringUtils.isNotBlank(mail.getSubject())) {
            b.getNotifiers().add(authenticationRiskEmailNotifier());
        }

        final SmsProperties sms = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getSms();
        if (StringUtils.isNotBlank(sms.getText()) && StringUtils.isNotBlank(sms.getFrom())) {
            b.getNotifiers().add(authenticationRiskSmsNotifier());
        }
    }
}
