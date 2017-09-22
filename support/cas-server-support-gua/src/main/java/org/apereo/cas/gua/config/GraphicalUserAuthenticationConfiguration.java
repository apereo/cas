package org.apereo.cas.gua.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.gua.GraphicalUserAuthenticationProperties;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.gua.impl.LdapUserGraphicalAuthenticationRepository;
import org.apereo.cas.gua.impl.StaticUserGraphicalAuthenticationRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.AcceptUserGraphicsForAuthenticationAction;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.DisplayUserGraphicsBeforeAuthenticationAction;
import org.apereo.cas.web.flow.GraphicalUserAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.PrepareForGraphicalAuthenticationAction;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link GraphicalUserAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("graphicalUserAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class GraphicalUserAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @ConditionalOnMissingBean(name = "graphicalUserAuthenticationWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer graphicalUserAuthenticationWebflowConfigurer() {
        return new GraphicalUserAuthenticationWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "userGraphicalAuthenticationRepository")
    public UserGraphicalAuthenticationRepository userGraphicalAuthenticationRepository() {

        final GraphicalUserAuthenticationProperties gua = casProperties.getAuthn().getGua();
        if (StringUtils.isNotBlank(gua.getResource().getLocation())) {
            return new StaticUserGraphicalAuthenticationRepository();
        }

        if (StringUtils.isNotBlank(gua.getLdap().getLdapUrl())
                && StringUtils.isNotBlank(gua.getLdap().getUserFilter())
                && StringUtils.isNotBlank(gua.getLdap().getBaseDn())
                && StringUtils.isNotBlank(gua.getLdap().getImageAttribute())) {
            return new LdapUserGraphicalAuthenticationRepository();
        }

        throw new BeanCreationException("A repository instance must be configured to locate user-defined graphics");

    }

    @Bean
    @ConditionalOnMissingBean(name = "acceptUserGraphicsForAuthenticationAction")
    public Action acceptUserGraphicsForAuthenticationAction() {
        return new AcceptUserGraphicsForAuthenticationAction();
    }

    @Autowired
    @Bean
    public Action displayUserGraphicsBeforeAuthenticationAction(@Qualifier("userGraphicalAuthenticationRepository")
                                                                final UserGraphicalAuthenticationRepository repository) {
        return new DisplayUserGraphicsBeforeAuthenticationAction(repository);
    }

    @Bean
    public Action initializeLoginAction() {
        return new PrepareForGraphicalAuthenticationAction(servicesManager);
    }
}
