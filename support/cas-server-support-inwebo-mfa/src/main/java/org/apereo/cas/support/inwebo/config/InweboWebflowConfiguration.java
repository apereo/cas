package org.apereo.cas.support.inwebo.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.support.inwebo.web.flow.InweboMultifactorAuthenticationWebflowEventResolver;
import org.apereo.cas.support.inwebo.web.flow.InweboMultifactorTrustWebflowConfigurer;
import org.apereo.cas.support.inwebo.web.flow.InweboMultifactorWebflowConfigurer;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboCheckAuthenticationAction;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboCheckUserAction;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboMustEnrollAction;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboPushAuthenticateAction;
import org.apereo.cas.trusted.config.ConditionalOnMultifactorTrustedDevicesEnabled;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.StaticEventExecutionAction;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * The Inwebo MFA webflow configuration.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Configuration("inweboWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class InweboWebflowConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("inweboService")
    private ObjectProvider<InweboService> inweboService;

    @Autowired
    @Qualifier("flowBuilder")
    private ObjectProvider<FlowBuilder> flowBuilder;

    @Autowired
    @Qualifier("casWebflowConfigurationContext")
    private ObjectProvider<CasWebflowEventResolutionConfigurationContext> casWebflowConfigurationContext;


    @Bean
    public FlowDefinitionRegistry inweboFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices.getObject());
        builder.addFlowBuilder(flowBuilder.getObject(), InweboMultifactorWebflowConfigurer.MFA_INWEBO_EVENT_ID);
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "inweboMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    @RefreshScope
    public CasWebflowConfigurer inweboMultifactorWebflowConfigurer() {
        val cfg = new InweboMultifactorWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            inweboFlowRegistry(),
            applicationContext,
            casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
        return cfg;
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer inweboCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(inweboMultifactorWebflowConfigurer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboMultifactorAuthenticationWebflowEventResolver")
    @RefreshScope
    public CasWebflowEventResolver inweboMultifactorAuthenticationWebflowEventResolver() {
        return new InweboMultifactorAuthenticationWebflowEventResolver(casWebflowConfigurationContext.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboPushAuthenticateAction")
    @RefreshScope
    public Action inweboPushAuthenticateAction() {
        return new InweboPushAuthenticateAction(inweboService.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboCheckUserAction")
    @RefreshScope
    public Action inweboCheckUserAction() {
        return new InweboCheckUserAction(inweboService.getObject(), casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboMustEnrollAction")
    @RefreshScope
    public Action inweboMustEnrollAction() {
        return new InweboMustEnrollAction();
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboCheckAuthenticationAction")
    @RefreshScope
    public Action inweboCheckAuthenticationAction() {
        return new InweboCheckAuthenticationAction(inweboService.getObject(), inweboMultifactorAuthenticationWebflowEventResolver());
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboSuccessAction")
    @RefreshScope
    public Action inweboSuccessAction() {
        return StaticEventExecutionAction.SUCCESS;
    }

    /**
     * The Inwebo multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnMultifactorTrustedDevicesEnabled(prefix = "cas.authn.mfa.inwebo")
    @Configuration("inweboMultifactorTrustConfiguration")
    public class InweboMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "inweboMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn({"defaultWebflowConfigurer", "inweboMultifactorWebflowConfigurer"})
        @RefreshScope
        public CasWebflowConfigurer inweboMultifactorTrustWebflowConfigurer() {
            val cfg = new InweboMultifactorTrustWebflowConfigurer(flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(),
                inweboFlowRegistry(),
                applicationContext,
                casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @Bean
        public CasWebflowExecutionPlanConfigurer inweboMultifactorTrustCasWebflowExecutionPlanConfigurer() {
            return plan -> plan.registerWebflowConfigurer(inweboMultifactorTrustWebflowConfigurer());
        }
    }
}
