package org.apereo.cas.support.saml.services.config;

import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServicesManagerExecutionPlanConfigurer;
import org.apereo.cas.support.saml.services.SamlServicesManager;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.stream.Collectors;



/**
 * This is {@link SamlServicesManagerConfiguration}.
 *
 * @author Dmitriy Kopylenko
 * @since 6.2.0
 */
@Configuration("casSamlServicesManagerConfiguration")
public class SamlServicesManagerConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Autowired
    @Qualifier("serviceRegistry")
    private ObjectProvider<ServiceRegistry> serviceRegistry;

    @Bean
    @ConditionalOnMissingBean(name = "samlServicesManagerExecutionPlanConfigurer")
    public ServicesManagerExecutionPlanConfigurer samlServicesManagerExecutionPlanConfigurer() {
        return () -> {
            val activeProfiles = Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toSet());
            return new SamlServicesManager(serviceRegistry.getObject(), applicationContext, activeProfiles);
        };
    }
}
