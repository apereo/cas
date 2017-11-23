package org.apereo.cas.config;

import org.apereo.cas.audit.spi.AuditPrincipalIdProvider;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.SurrogateAuthenticationPostProcessor;
import org.apereo.cas.authentication.SurrogatePrincipalResolver;
import org.apereo.cas.authentication.audit.SurrogateAuditPrincipalIdProvider;
import org.apereo.cas.authentication.event.SurrogateAuthenticationEventListener;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.surrogate.JsonResourceSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.SurrogateSessionExpirationPolicy;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link SurrogateAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author John Gasper
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("surrogateAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SurrogateAuthenticationConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateAuthenticationConfiguration.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @Bean
    public ExpirationPolicy grantingTicketExpirationPolicy(@Qualifier("ticketGrantingTicketExpirationPolicy") 
                                                           final ExpirationPolicy ticketGrantingTicketExpirationPolicy) {
        final SurrogateAuthenticationProperties su = casProperties.getAuthn().getSurrogate();
        final HardTimeoutExpirationPolicy surrogatePolicy = new HardTimeoutExpirationPolicy(su.getTgt().getTimeToKillInSeconds());
        final SurrogateSessionExpirationPolicy policy = new SurrogateSessionExpirationPolicy(surrogatePolicy);
        policy.addPolicy(SurrogateSessionExpirationPolicy.PolicyTypes.SURROGATE, surrogatePolicy);
        policy.addPolicy(SurrogateSessionExpirationPolicy.PolicyTypes.DEFAULT, ticketGrantingTicketExpirationPolicy);
        return policy;
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "surrogateAuthenticationService")
    @Bean
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        try {
            final SurrogateAuthenticationProperties su = casProperties.getAuthn().getSurrogate();
            if (su.getJson().getLocation() != null) {
                LOGGER.debug("Using JSON resource [{}] to locate surrogate accounts", su.getJson().getLocation());
                return new JsonResourceSurrogateAuthenticationService(su.getJson().getLocation(), servicesManager);
            }

            final Map<String, Set> accounts = new LinkedHashMap<>();
            su.getSimple().getSurrogates().forEach((k, v) -> accounts.put(k, StringUtils.commaDelimitedListToSet(v)));
            LOGGER.debug("Using accounts [{}] for surrogate authentication", accounts);
            return new SimpleSurrogateAuthenticationService(accounts, servicesManager);
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Autowired
    @RefreshScope
    @Bean
    public PrincipalResolver personDirectoryPrincipalResolver(@Qualifier("attributeRepository") final IPersonAttributeDao attributeRepository,
                                                              @Qualifier("principalFactory") final PrincipalFactory principalFactory) {
        return new SurrogatePrincipalResolver(attributeRepository, principalFactory,
                casProperties.getPersonDirectory().isReturnNull(), casProperties.getPersonDirectory().getPrincipalAttribute());
    }

    @Bean
    public AuthenticationPostProcessor surrogateAuthenticationPostProcessor() {
        return new SurrogateAuthenticationPostProcessor(new DefaultPrincipalFactory(), surrogateAuthenticationService(),
                servicesManager, eventPublisher);
    }

    @Bean
    public AuditPrincipalIdProvider auditPrincipalIdProvider() {
        return new SurrogateAuditPrincipalIdProvider();
    }

    @ConditionalOnMissingBean(name = "surrogateAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationPostProcessor(surrogateAuthenticationPostProcessor());
    }

    @ConditionalOnMissingBean(name = "surrogateAuthenticationEventListener")
    @Bean
    public SurrogateAuthenticationEventListener surrogateAuthenticationEventListener() {
        return new SurrogateAuthenticationEventListener(communicationsManager, casProperties);
    }
}
