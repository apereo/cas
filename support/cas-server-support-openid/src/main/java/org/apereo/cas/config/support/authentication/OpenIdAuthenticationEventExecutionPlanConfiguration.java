package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.openid.authentication.handler.support.OpenIdCredentialsAuthenticationHandler;
import org.apereo.cas.support.openid.authentication.principal.OpenIdPrincipalResolver;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.val;
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
 * @since 5.1.0
 * @deprecated 6.2
 */
@Configuration("openIdAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Deprecated(since = "6.2.0")
public class OpenIdAuthenticationEventExecutionPlanConfiguration {
    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
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
        return CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(openidPrincipalFactory(),
            attributeRepository.getObject(),
            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()),
            OpenIdPrincipalResolver.class,
            principal, personDirectory);
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
