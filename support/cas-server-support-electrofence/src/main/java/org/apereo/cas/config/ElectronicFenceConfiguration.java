package org.apereo.cas.config;

import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskContingencyPlan;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskMitigator;
import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.impl.calcs.DateTimeAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.calcs.GeoLocationAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.calcs.IpAddressAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.calcs.UserAgentAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.engine.DefaultAuthenticationRiskEvaluator;
import org.apereo.cas.impl.engine.DefaultAuthenticationRiskMitigator;
import org.apereo.cas.impl.notify.AuthenticationRiskEmailNotifier;
import org.apereo.cas.impl.notify.AuthenticationRiskSmsNotifier;
import org.apereo.cas.impl.plans.BaseAuthenticationRiskContingencyPlan;
import org.apereo.cas.impl.plans.BlockAuthenticationContingencyPlan;
import org.apereo.cas.impl.plans.MultifactorAuthenticationContingencyPlan;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.support.events.CasEventRepository;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashSet;

/**
 * This is {@link ElectronicFenceConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@Slf4j
@Configuration(value = "electronicFenceConfiguration", proxyBeanMethods = false)
public class ElectronicFenceConfiguration {

    private static void configureContingencyPlan(final BaseAuthenticationRiskContingencyPlan b,
                                                 final CasConfigurationProperties casProperties,
                                                 final AuthenticationRiskNotifier authenticationRiskEmailNotifier,
                                                 final AuthenticationRiskNotifier authenticationRiskSmsNotifier) {
        val response = casProperties.getAuthn().getAdaptive().getRisk().getResponse();
        val mail = response.getMail();
        if (mail.isDefined()) {
            b.getNotifiers().add(authenticationRiskEmailNotifier);
        }
        val sms = response.getSms();
        if (sms.isDefined()) {
            b.getNotifiers().add(authenticationRiskSmsNotifier);
        }
    }

    @ConditionalOnMissingBean(name = "authenticationRiskEmailNotifier")
    @Bean
    @RefreshScope
    @Autowired
    public AuthenticationRiskNotifier authenticationRiskEmailNotifier(final CasConfigurationProperties casProperties,
                                                                      @Qualifier("communicationsManager")
                                                                      final CommunicationsManager communicationsManager) {
        return new AuthenticationRiskEmailNotifier(casProperties, communicationsManager);
    }

    @ConditionalOnMissingBean(name = "authenticationRiskSmsNotifier")
    @Bean
    @RefreshScope
    @Autowired
    public AuthenticationRiskNotifier authenticationRiskSmsNotifier(final CasConfigurationProperties casProperties,
                                                                    @Qualifier("communicationsManager")
                                                                    final CommunicationsManager communicationsManager) {
        return new AuthenticationRiskSmsNotifier(casProperties, communicationsManager);
    }

    @ConditionalOnMissingBean(name = "blockAuthenticationContingencyPlan")
    @Bean
    @RefreshScope
    @Autowired
    public AuthenticationRiskContingencyPlan blockAuthenticationContingencyPlan(final CasConfigurationProperties casProperties,
                                                                                final ConfigurableApplicationContext applicationContext,
                                                                                @Qualifier("authenticationRiskEmailNotifier")
                                                                                final AuthenticationRiskNotifier authenticationRiskEmailNotifier,
                                                                                @Qualifier("authenticationRiskSmsNotifier")
                                                                                final AuthenticationRiskNotifier authenticationRiskSmsNotifier) {
        val b = new BlockAuthenticationContingencyPlan(casProperties, applicationContext);
        configureContingencyPlan(b, casProperties, authenticationRiskEmailNotifier, authenticationRiskSmsNotifier);
        return b;
    }

    @ConditionalOnMissingBean(name = "multifactorAuthenticationContingencyPlan")
    @Bean
    @RefreshScope
    @Autowired
    public AuthenticationRiskContingencyPlan multifactorAuthenticationContingencyPlan(final CasConfigurationProperties casProperties,
                                                                                      final ConfigurableApplicationContext applicationContext,
                                                                                      @Qualifier("authenticationRiskEmailNotifier")
                                                                                      final AuthenticationRiskNotifier authenticationRiskEmailNotifier,
                                                                                      @Qualifier("authenticationRiskSmsNotifier")
                                                                                      final AuthenticationRiskNotifier authenticationRiskSmsNotifier) {
        val b = new MultifactorAuthenticationContingencyPlan(casProperties, applicationContext);
        configureContingencyPlan(b, casProperties, authenticationRiskEmailNotifier, authenticationRiskSmsNotifier);
        return b;
    }

    @ConditionalOnMissingBean(name = "authenticationRiskMitigator")
    @Bean
    @RefreshScope
    @Autowired
    public AuthenticationRiskMitigator authenticationRiskMitigator(final CasConfigurationProperties casProperties,
                                                                   @Qualifier("blockAuthenticationContingencyPlan")
                                                                   final AuthenticationRiskContingencyPlan blockAuthenticationContingencyPlan,
                                                                   @Qualifier("multifactorAuthenticationContingencyPlan")
                                                                   final AuthenticationRiskContingencyPlan multifactorAuthenticationContingencyPlan) {
        if (casProperties.getAuthn().getAdaptive().getRisk().getResponse().isBlockAttempt()) {
            return new DefaultAuthenticationRiskMitigator(blockAuthenticationContingencyPlan);
        }
        return new DefaultAuthenticationRiskMitigator(multifactorAuthenticationContingencyPlan);
    }

    @ConditionalOnMissingBean(name = "ipAddressAuthenticationRequestRiskCalculator")
    @Bean
    @RefreshScope
    @Autowired
    public AuthenticationRequestRiskCalculator ipAddressAuthenticationRequestRiskCalculator(final CasConfigurationProperties casProperties,
                                                                                            @Qualifier("casEventRepository")
                                                                                            final CasEventRepository casEventRepository) {
        return new IpAddressAuthenticationRequestRiskCalculator(casEventRepository, casProperties);
    }

    @ConditionalOnMissingBean(name = "userAgentAuthenticationRequestRiskCalculator")
    @Bean
    @RefreshScope
    @Autowired
    public AuthenticationRequestRiskCalculator userAgentAuthenticationRequestRiskCalculator(final CasConfigurationProperties casProperties,
                                                                                            @Qualifier("casEventRepository")
                                                                                            final CasEventRepository casEventRepository) {
        return new UserAgentAuthenticationRequestRiskCalculator(casEventRepository, casProperties);
    }

    @ConditionalOnMissingBean(name = "dateTimeAuthenticationRequestRiskCalculator")
    @Bean
    @RefreshScope
    @Autowired
    public AuthenticationRequestRiskCalculator dateTimeAuthenticationRequestRiskCalculator(final CasConfigurationProperties casProperties,
                                                                                           @Qualifier("casEventRepository")
                                                                                           final CasEventRepository casEventRepository) {
        return new DateTimeAuthenticationRequestRiskCalculator(casEventRepository, casProperties);
    }

    @ConditionalOnMissingBean(name = "geoLocationAuthenticationRequestRiskCalculator")
    @Bean
    @RefreshScope
    @Autowired
    public AuthenticationRequestRiskCalculator geoLocationAuthenticationRequestRiskCalculator(final CasConfigurationProperties casProperties,
                                                                                              @Qualifier("geoLocationService")
                                                                                              final GeoLocationService geoLocationService,
                                                                                              @Qualifier("casEventRepository")
                                                                                              final CasEventRepository casEventRepository) {
        return new GeoLocationAuthenticationRequestRiskCalculator(casEventRepository, casProperties, geoLocationService);
    }

    @ConditionalOnMissingBean(name = "authenticationRiskEvaluator")
    @Bean
    @RefreshScope
    @Autowired
    public AuthenticationRiskEvaluator authenticationRiskEvaluator(final CasConfigurationProperties casProperties,
                                                                   @Qualifier("ipAddressAuthenticationRequestRiskCalculator")
                                                                   final AuthenticationRequestRiskCalculator ipAddressAuthenticationRequestRiskCalculator,
                                                                   @Qualifier("userAgentAuthenticationRequestRiskCalculator")
                                                                   final AuthenticationRequestRiskCalculator userAgentAuthenticationRequestRiskCalculator,
                                                                   @Qualifier("dateTimeAuthenticationRequestRiskCalculator")
                                                                   final AuthenticationRequestRiskCalculator dateTimeAuthenticationRequestRiskCalculator,
                                                                   @Qualifier("geoLocationAuthenticationRequestRiskCalculator")
                                                                   final AuthenticationRequestRiskCalculator geoLocationAuthenticationRequestRiskCalculator) {
        val risk = casProperties.getAuthn().getAdaptive().getRisk();
        val calculators = new HashSet<AuthenticationRequestRiskCalculator>();
        if (risk.getIp().isEnabled()) {
            calculators.add(ipAddressAuthenticationRequestRiskCalculator);
        }
        if (risk.getAgent().isEnabled()) {
            calculators.add(userAgentAuthenticationRequestRiskCalculator);
        }
        if (risk.getDateTime().isEnabled()) {
            calculators.add(dateTimeAuthenticationRequestRiskCalculator);
        }
        if (risk.getGeoLocation().isEnabled()) {
            calculators.add(geoLocationAuthenticationRequestRiskCalculator);
        }
        if (calculators.isEmpty()) {
            LOGGER.warn("No risk calculators are defined to examine authentication requests");
        }
        return new DefaultAuthenticationRiskEvaluator(calculators);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casElectrofenceAuditTrailRecordResolutionPlanConfigurer")
    @Autowired
    public AuditTrailRecordResolutionPlanConfigurer casElectrofenceAuditTrailRecordResolutionPlanConfigurer(
        @Qualifier("returnValueResourceResolver")
        final AuditResourceResolver returnValueResourceResolver) {
        return plan -> {
            plan.registerAuditActionResolver(AuditActionResolvers.ADAPTIVE_RISKY_AUTHENTICATION_ACTION_RESOLVER, new DefaultAuditActionResolver());
            plan.registerAuditResourceResolver(AuditResourceResolvers.ADAPTIVE_RISKY_AUTHENTICATION_RESOURCE_RESOLVER, returnValueResourceResolver);
        };
    }
}
