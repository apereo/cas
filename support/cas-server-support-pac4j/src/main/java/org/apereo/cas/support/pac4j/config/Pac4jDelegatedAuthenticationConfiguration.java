package org.apereo.cas.support.pac4j.config;

import java.util.function.Function;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.pac4j.web.flow.DelegatedClientAuthenticationAction;
import org.pac4j.core.client.Clients;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.service.InMemoryProfileService;
import org.pac4j.core.profile.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link Pac4jDelegatedAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("pac4jDelegatedAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class Pac4jDelegatedAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;
    
    @Autowired
    @RefreshScope
    @Bean
    @Lazy
    public Action clientAction(@Qualifier("builtClients") final Clients builtClients) {
        return new DelegatedClientAuthenticationAction(builtClients, 
                authenticationSystemSupport, 
                centralAuthenticationService, 
                casProperties.getTheme().getParamName(), 
                casProperties.getLocale().getParamName(), 
                casProperties.getAuthn().getPac4j().isAutoRedirect(),
                pac4jProfileService());
    }


    /**
     * Provides a PAC4J profile service able to save and retrieve user profiles.
     * 
     * This implementation uses {@link InMemoryProfileService}, which stores profiles in a map in memory.
     * It will work fine on a single node but not in a cluster. For cluster deployments, use a different implementation of
     * {@link ProfileService}, such an implementation that is able to persist and share {@link CommonProfile} across nodes.
     * 
     * @return A PAC4J profile service.
     */
    @Bean
    @ConditionalOnMissingBean(name = "pac4jProfileService")
    public ProfileService<CommonProfile> pac4jProfileService() {
        final Function<Object[], CommonProfile> profileFactory = x -> new CommonProfile();
        return new InMemoryProfileService<>(profileFactory);
    }

}
