package org.apereo.cas.config;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskContingencyPlan;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskMitigator;
import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.RiskBasedAuthenticationProperties;
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
import org.apereo.cas.support.events.dao.CasEventRepository;
import org.apereo.cas.web.flow.RiskAwareAuthenticationWebflowEventResolver;
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

import java.util.Set;

/**
 * This is {@link ElectronicFenceConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("electroFenceConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class ElectronicFenceConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElectronicFenceConfiguration.class);

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository casEventRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "authenticationRiskEmailNotifier")
    @Bean
    @RefreshScope
    public AuthenticationRiskNotifier authenticationRiskEmailNotifier() {
        return new AuthenticationRiskEmailNotifier();
    }

    @ConditionalOnMissingBean(name = "authenticationRiskSmsNotifier")
    @Bean
    @RefreshScope
    public AuthenticationRiskNotifier authenticationRiskSmsNotifier() {
        return new AuthenticationRiskTwilioSmsNotifier();
    }

    @ConditionalOnMissingBean(name = "riskAwareAuthenticationWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver riskAwareAuthenticationWebflowEventResolver() {
        return new RiskAwareAuthenticationWebflowEventResolver(authenticationRiskEvaluator(), authenticationRiskMitigator());
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
        return new DateTimeAuthenticationRequestRiskCalculator(this.casEventRepository);
    }

    @ConditionalOnMissingBean(name = "geoLocationAuthenticationRequestRiskCalculator")
    @Bean
    @RefreshScope
    public AuthenticationRequestRiskCalculator geoLocationAuthenticationRequestRiskCalculator() {
        return new GeoLocationAuthenticationRequestRiskCalculator(this.casEventRepository);
    }

    @ConditionalOnMissingBean(name = "authenticationRiskEvaluator")
    @Bean
    @RefreshScope
    public AuthenticationRiskEvaluator authenticationRiskEvaluator() {
        final Set<AuthenticationRequestRiskCalculator> calculators = Sets.newHashSet();

        if (casProperties.getAuthn().getAdaptive().getRisk().getIp().isEnabled()) {
            calculators.add(ipAddressAuthenticationRequestRiskCalculator());
        }
        if (casProperties.getAuthn().getAdaptive().getRisk().getAgent().isEnabled()) {
            calculators.add(userAgentAuthenticationRequestRiskCalculator());
        }
        if (casProperties.getAuthn().getAdaptive().getRisk().getDateTime().isEnabled()) {
            calculators.add(dateTimeAuthenticationRequestRiskCalculator());
        }
        if (casProperties.getAuthn().getAdaptive().getRisk().getGeoLocation().isEnabled()) {
            calculators.add(geoLocationAuthenticationRequestRiskCalculator());
        }

        if (calculators.isEmpty()) {
            LOGGER.warn("No risk calculators are define to examine authentication requests");
        }

        return new DefaultAuthenticationRiskEvaluator(calculators);
    }

    private void configureContingencyPlan(final BaseAuthenticationRiskContingencyPlan b) {
        final RiskBasedAuthenticationProperties.Response.Mail mail =
                casProperties.getAuthn().getAdaptive().getRisk().getResponse().getMail();

        final RiskBasedAuthenticationProperties.Response.Sms sms =
                casProperties.getAuthn().getAdaptive().getRisk().getResponse().getSms();

        if (StringUtils.isNotBlank(mail.getText())
                && StringUtils.isNotBlank(mail.getFrom())
                && StringUtils.isNotBlank(mail.getSubject())) {
            b.getNotifiers().add(authenticationRiskEmailNotifier());
        }

        if (StringUtils.isNotBlank(sms.getText())
                && StringUtils.isNotBlank(sms.getFrom())
                && StringUtils.isNotBlank(sms.getTwilio().getToken())
                && StringUtils.isNotBlank(sms.getTwilio().getAccountId())) {
            b.getNotifiers().add(authenticationRiskSmsNotifier());
        }
    }
}
