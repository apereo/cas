package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.openid.OpenIdProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.openid.authentication.handler.support.OpenIdCredentialsAuthenticationHandler;
import org.apereo.cas.support.openid.authentication.principal.OpenIdPrincipalResolver;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link OpenIdAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("openIdAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OpenIdAuthenticationEventExecutionPlanConfiguration {
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;
    
    @Bean
    public AuthenticationHandler openIdCredentialsAuthenticationHandler() {
        final OpenIdProperties openid = casProperties.getAuthn().getOpenid();
        return new OpenIdCredentialsAuthenticationHandler(openid.getName(), servicesManager, openidPrincipalFactory(), ticketRegistry);
    }

    @Bean
    public OpenIdPrincipalResolver openIdPrincipalResolver() {
        final OpenIdPrincipalResolver r = new OpenIdPrincipalResolver(attributeRepository, openidPrincipalFactory(),
                casProperties.getAuthn().getOpenid().getPrincipal().isReturnNull(),
                casProperties.getAuthn().getOpenid().getPrincipal().getPrincipalAttribute());
        return r;
    }

    @ConditionalOnMissingBean(name = "openidPrincipalFactory")
    @Bean
    public PrincipalFactory openidPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "openIdAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer openIdAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(openIdCredentialsAuthenticationHandler(), openIdPrincipalResolver());
    }
}
