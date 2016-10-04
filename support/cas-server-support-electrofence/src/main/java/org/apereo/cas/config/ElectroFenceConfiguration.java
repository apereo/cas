package org.apereo.cas.config;

import com.google.common.collect.Sets;
import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskMitigator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.impl.calcs.DateTimeAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.calcs.GeoLocationAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.calcs.IpAddressAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.calcs.UserAgentAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.engine.DefaultAuthenticationRiskEvaluator;
import org.apereo.cas.impl.engine.DefaultAuthenticationRiskMitigator;
import org.apereo.cas.impl.plans.BlockAuthenticationContingencyPlan;
import org.apereo.cas.impl.plans.MultifactorAuthenticationContingencyPlan;
import org.apereo.cas.web.flow.RiskAwareAuthenticationWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * This is {@link ElectroFenceConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("electroFenceConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class ElectroFenceConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElectroFenceConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public CasWebflowEventResolver riskAwareAuthenticationWebflowEventResolver() {
        return new RiskAwareAuthenticationWebflowEventResolver(authenticationRiskEvaluator(), authenticationRiskMitigator());
    }
    
    @Bean
    @RefreshScope
    public AuthenticationRiskMitigator authenticationRiskMitigator() {
        if (casProperties.getAuthn().getAdaptive().getRisk().getResponse().isBlockAttempt()) {
            return new DefaultAuthenticationRiskMitigator(new BlockAuthenticationContingencyPlan());
        }
        return new DefaultAuthenticationRiskMitigator(new MultifactorAuthenticationContingencyPlan()); 
    }
    @Bean
    @RefreshScope
    public AuthenticationRiskEvaluator authenticationRiskEvaluator() {
        final Set<AuthenticationRequestRiskCalculator> calcs = Sets.newHashSet();

        if (casProperties.getAuthn().getAdaptive().getRisk().getIp().isEnabled()) {
            calcs.add(new IpAddressAuthenticationRequestRiskCalculator());
        }
        if (casProperties.getAuthn().getAdaptive().getRisk().getAgent().isEnabled()) {
            calcs.add(new UserAgentAuthenticationRequestRiskCalculator());
        }
        if (casProperties.getAuthn().getAdaptive().getRisk().getDateTime().isEnabled()) {
            calcs.add(new DateTimeAuthenticationRequestRiskCalculator());
        }
        if (casProperties.getAuthn().getAdaptive().getRisk().getGeoLocation().isEnabled()) {
            calcs.add(new GeoLocationAuthenticationRequestRiskCalculator());
        }

        if (calcs.isEmpty()) {
            LOGGER.warn("No risk calculators are define to examine authentication requests");
        }

        return new DefaultAuthenticationRiskEvaluator(calcs);
    }

}
