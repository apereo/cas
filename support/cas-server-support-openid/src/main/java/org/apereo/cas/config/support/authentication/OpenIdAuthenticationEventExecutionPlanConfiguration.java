package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.openid.authentication.handler.support.OpenIdCredentialsAuthenticationHandler;
import org.apereo.cas.support.openid.authentication.principal.OpenIdPrincipalResolver;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.ObjectProvider;
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
 * @deprecated 6.2
 * @since 5.1.0
 */
@Configuration("openIdAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Deprecated(since = "6.2.0")
public class OpenIdAuthenticationEventExecutionPlanConfiguration {
    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("attributeRepository")
    private ObjectProvider<IPersonAttributeDao> attributeRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Bean
    public AuthenticationHandler openIdCredentialsAuthenticationHandler() {
        val openid = casProperties.getAuthn().getOpenid();
        return new OpenIdCredentialsAuthenticationHandler(openid.getName(), servicesManager.getObject(),
            openidPrincipalFactory(), ticketRegistry.getObject(),
            openid.getOrder());
    }

    @Bean
    public OpenIdPrincipalResolver openIdPrincipalResolver() {
        val personDirectory = casProperties.getPersonDirectory();
        val principal = casProperties.getAuthn().getOpenid().getPrincipal();
        val principalAttribute = StringUtils.defaultIfBlank(principal.getPrincipalAttribute(), personDirectory.getPrincipalAttribute());
        return new OpenIdPrincipalResolver(attributeRepository.getObject(),
            openidPrincipalFactory(),
            principal.isReturnNull() || personDirectory.isReturnNull(),
            principalAttribute,
            principal.isUseExistingPrincipalId() || personDirectory.isUseExistingPrincipalId(),
            principal.isAttributeResolutionEnabled(),
            org.springframework.util.StringUtils.commaDelimitedListToSet(principal.getActiveAttributeRepositoryIds()));
    }

    @ConditionalOnMissingBean(name = "openidPrincipalFactory")
    @Bean
    public PrincipalFactory openidPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "openIdAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer openIdAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(openIdCredentialsAuthenticationHandler(), openIdPrincipalResolver());
    }
}
