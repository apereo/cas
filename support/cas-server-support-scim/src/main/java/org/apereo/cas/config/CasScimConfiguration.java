package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.scim.ScimProperties;
import org.apereo.cas.scim.api.ScimProvisioner;
import org.apereo.cas.scim.v1.Scim1PrincipalAttributeMapper;
import org.apereo.cas.scim.v1.Scim1Provisioner;
import org.apereo.cas.scim.v2.Scim2PrincipalAttributeMapper;
import org.apereo.cas.scim.v2.Scim2Provisioner;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.PrincipalScimProvisionerAction;
import org.apereo.cas.web.flow.ScimWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasScimConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casScimConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class CasScimConfiguration {
    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;
    
    @ConditionalOnMissingBean(name = "scimWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer scimWebflowConfigurer() {
        final CasWebflowConfigurer w = new ScimWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        w.initialize();
        return w;
    }

    @Bean
    public Scim2PrincipalAttributeMapper scim2PrincipalAttributeMapper() {
        return new Scim2PrincipalAttributeMapper();
    }

    @Bean
    public Scim1PrincipalAttributeMapper scim1PrincipalAttributeMapper() {
        return new Scim1PrincipalAttributeMapper();
    }

    @Bean
    public ScimProvisioner scimProvisioner() {
        final ScimProperties scim = casProperties.getScim();
        if (casProperties.getScim().getVersion() == 1) {
            return new Scim1Provisioner(scim.getTarget(),
                    scim.getOauthToken(), scim.getUsername(),
                    scim.getPassword(),
                    scim1PrincipalAttributeMapper());
        }
        return new Scim2Provisioner(scim.getTarget(),
                scim.getOauthToken(), scim.getUsername(), scim.getPassword(),
                scim2PrincipalAttributeMapper());
    }

    @Bean
    public Action principalScimProvisionerAction() {
        return new PrincipalScimProvisionerAction(scimProvisioner());
    }
}
