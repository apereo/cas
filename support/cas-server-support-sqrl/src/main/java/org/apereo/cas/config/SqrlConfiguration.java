package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.sqrl.SqrlAuthenticationProperties;
import org.apereo.cas.sqrl.storage.SqrlInMemoryAuthenticationService;
import org.apereo.cas.sqrl.storage.SqrlInMemoryUserService;
import org.apereo.cas.web.SqrlAuthenticationController;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.SqrlCleanUpAction;
import org.apereo.cas.web.flow.SqrlInitialAction;
import org.apereo.cas.web.flow.SqrlWebflowConfigurer;
import org.jsqrl.config.SqrlConfig;
import org.jsqrl.server.JSqrlServer;
import org.jsqrl.service.SqrlAuthenticationService;
import org.jsqrl.service.SqrlNutService;
import org.jsqrl.service.SqrlUserService;
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

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * This is {@link SqrlConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("SqrlConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SqrlConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Bean
    @ConditionalOnMissingBean(name = "sqrlWebflowConfigurer")
    public CasWebflowConfigurer sqrlWebflowConfigurer() {
        return new SqrlWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "sqrlAuthenticationController")
    public SqrlAuthenticationController sqrlAuthenticationController() {
        return new SqrlAuthenticationController(sqrlServer());
    }


    @Bean
    public Action sqrlCleanUpAction() {
        return new SqrlCleanUpAction();
    }

    @Bean
    public Action sqrlInitialAction() {
        return new SqrlInitialAction(sqrlConfig(), sqrlServer());
    }

    @Bean
    public SqrlNutService sqrlNutService() {
        try {
            return new SqrlNutService(new SecureRandom(), sqrlConfig(),
                    MessageDigest.getInstance("SHA-256"), sqrlAesKey());
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    public JSqrlServer sqrlServer() {
        return new JSqrlServer(sqrlUserService(), sqrlAuthenticationService(),
                sqrlConfig(), sqrlNutService());
    }

    @Bean
    public SqrlUserService sqrlUserService() {
        return new SqrlInMemoryUserService();
    }

    @Bean
    public SqrlAuthenticationService sqrlAuthenticationService() {
        return new SqrlInMemoryAuthenticationService();
    }

    @Bean
    public SqrlConfig sqrlConfig() {
        final SqrlAuthenticationProperties sqrl = casProperties.getAuthn().getSqrl();

        final SqrlConfig sqrlConfig = new SqrlConfig();
        sqrlConfig.setIpAddressRequired(true);
        sqrlConfig.setSqrlVersion("1");
        sqrlConfig.setSfn(sqrl.getSfn());
        sqrlConfig.setNutExpirationSeconds(sqrl.getNutExpirationSeconds());
        sqrlConfig.setSqrlBaseUri("/cas/sqrl");
        return sqrlConfig;
    }

    @Bean
    public Key sqrlAesKey() {
        try {
            final KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            return keyGen.generateKey();
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }
}
