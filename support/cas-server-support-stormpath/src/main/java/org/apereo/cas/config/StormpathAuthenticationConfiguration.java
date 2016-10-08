package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.StormpathAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * This is {@link StormpathAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("stormpathAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class StormpathAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Bean
    public PrincipalFactory stormpathPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public AuthenticationHandler stormpathAuthenticationHandler() {
        final StormpathAuthenticationHandler handler =
                new StormpathAuthenticationHandler(
                        casProperties.getAuthn().getStormpath().getApiKey(),
                        casProperties.getAuthn().getStormpath().getApplicationId(),
                        casProperties.getAuthn().getStormpath().getSecretkey());

        handler.setPasswordEncoder(Beans.newPasswordEncoder(casProperties.getAuthn().getStormpath().getPasswordEncoder()));
        handler.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(casProperties.getAuthn().getStormpath().getPrincipalTransformation()));

        handler.setPrincipalFactory(stormpathPrincipalFactory());
        handler.setServicesManager(servicesManager);
        return handler;
    }

    @PostConstruct
    public void initializeAuthenticationHandler() {

        if (StringUtils.isNotBlank(casProperties.getAuthn().getStormpath().getApiKey())
            && StringUtils.isNotBlank(casProperties.getAuthn().getStormpath().getSecretkey())) {
            this.authenticationHandlersResolvers.put(stormpathAuthenticationHandler(),
                    personDirectoryPrincipalResolver);
        }
    }
}
