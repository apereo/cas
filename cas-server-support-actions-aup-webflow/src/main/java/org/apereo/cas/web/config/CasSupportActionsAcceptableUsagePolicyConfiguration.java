package org.apereo.cas.web.config;

import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.web.flow.AcceptableUsagePolicyFormAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicyRepository;
import org.apereo.cas.web.flow.AcceptableUsagePolicyWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.DefaultAcceptableUsagePolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasSupportActionsAcceptableUsagePolicyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSupportActionsAcceptableUsagePolicyConfiguration")
public class CasSupportActionsAcceptableUsagePolicyConfiguration {

    @Autowired
    private AcceptableUsagePolicyProperties properties;
    
    @Bean
    public Action acceptableUsagePolicyFormAction() {
        return new AcceptableUsagePolicyFormAction();
    }

    @Bean
    public CasWebflowConfigurer acceptableUsagePolicyWebflowConfigurer() {
        return new AcceptableUsagePolicyWebflowConfigurer();
    }

    @Bean
    public AcceptableUsagePolicyRepository defaultAcceptableUsagePolicyRepository() {
        return new DefaultAcceptableUsagePolicyRepository();
    }
}
