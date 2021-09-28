package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.openid.authentication.handler.support.OpenIdCredentialsAuthenticationHandler;
import org.apereo.cas.support.openid.authentication.principal.OpenIdPrincipalResolver;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.val;
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
 * @deprecated 6.2
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Deprecated(since = "6.2.0")
@Configuration(value = "openIdAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
public class OpenIdAuthenticationEventExecutionPlanConfiguration {

    @Bean
    @Autowired
    public AuthenticationHandler openIdCredentialsAuthenticationHandler(final CasConfigurationProperties casProperties,
                                                                        @Qualifier("openidPrincipalFactory")
                                                                        final PrincipalFactory openidPrincipalFactory,
                                                                        @Qualifier(ServicesManager.BEAN_NAME)
                                                                        final ServicesManager servicesManager,
                                                                        @Qualifier(TicketRegistry.BEAN_NAME)
                                                                        final TicketRegistry ticketRegistry) {
        val openid = casProperties.getAuthn().getOpenid();
        return new OpenIdCredentialsAuthenticationHandler(openid.getName(), servicesManager, openidPrincipalFactory, ticketRegistry, openid.getOrder());
    }

    @Bean
    @Autowired
    public OpenIdPrincipalResolver openIdPrincipalResolver(final CasConfigurationProperties casProperties,
                                                           @Qualifier("openidPrincipalFactory")
                                                           final PrincipalFactory openidPrincipalFactory,
                                                           @Qualifier("attributeRepository")
                                                           final IPersonAttributeDao attributeRepository) {
        val personDirectory = casProperties.getPersonDirectory();
        val principal = casProperties.getAuthn().getOpenid().getPrincipal();
        return CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(openidPrincipalFactory, attributeRepository,
            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()), OpenIdPrincipalResolver.class, principal, personDirectory);
    }

    @ConditionalOnMissingBean(name = "openidPrincipalFactory")
    @Bean
    public PrincipalFactory openidPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "openIdAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer openIdAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("openIdCredentialsAuthenticationHandler")
        final AuthenticationHandler openIdCredentialsAuthenticationHandler,
        @Qualifier("openIdPrincipalResolver")
        final OpenIdPrincipalResolver openIdPrincipalResolver) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(openIdCredentialsAuthenticationHandler, openIdPrincipalResolver);
    }
}
