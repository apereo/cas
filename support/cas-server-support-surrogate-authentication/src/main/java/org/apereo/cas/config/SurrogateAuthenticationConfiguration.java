package org.apereo.cas.config;

import org.apereo.cas.audit.spi.PrincipalIdProvider;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.SurrogateAuthenticationAspect;
import org.apereo.cas.authentication.SurrogatePrincipalResolver;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.audit.SurrogatePrincipalIdProvider;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.surrogate.JsonResourceSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.LdapSurrogateUsernamePasswordService;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.SurrogateInitialAuthenticationAction;
import org.apereo.cas.web.flow.SurrogateSelectionAction;
import org.apereo.cas.web.flow.SurrogateWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.ldaptive.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.StringUtils;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link SurrogateAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author John Gasper
 * @since 5.1.0
 */
@Configuration("surrogateAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAspectJAutoProxy
public class SurrogateAuthenticationConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateAuthenticationConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @ConditionalOnMissingBean(name = "surrogateWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer surrogateWebflowConfigurer() {
        return new SurrogateWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, selectSurrogateAction());
    }

    @ConditionalOnMissingBean(name = "selectSurrogateAction")
    @Bean
    public Action selectSurrogateAction() {
        return new SurrogateSelectionAction(casProperties.getAuthn().getSurrogate().getSeparator());
    }

    @Bean
    public Action authenticationViaFormAction() {
        return new SurrogateInitialAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                casProperties.getAuthn().getSurrogate().getSeparator(),
                surrogateAuthenticationService());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "surrogateAuthenticationService")
    @Bean
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        try {
            final SurrogateAuthenticationProperties su = casProperties.getAuthn().getSurrogate();
            if (su.getJson().getConfig().getLocation() != null) {
                LOGGER.debug("Using JSON resource [{}] to locate surrogate accounts", su.getJson().getConfig().getLocation());
                return new JsonResourceSurrogateAuthenticationService(su.getJson().getConfig().getLocation());
            }
            if (StringUtils.hasText(su.getLdap().getLdapUrl()) && StringUtils.hasText(su.getLdap().getSearchFilter())
                    && StringUtils.hasText(su.getLdap().getBaseDn()) && StringUtils.hasText(su.getLdap().getMemberAttributeName())) {
                LOGGER.debug("Using LDAP [{}] with baseDn [{}] to locate surrogate accounts",
                        su.getLdap().getLdapUrl(), su.getLdap().getBaseDn());
                final ConnectionFactory factory = Beans.newLdaptivePooledConnectionFactory(su.getLdap());
                return new LdapSurrogateUsernamePasswordService(factory, su.getLdap());
            }

            final Map<String, Set> accounts = new LinkedHashMap<>();
            su.getSimple().getSurrogates().forEach((k, v) -> accounts.put(k, StringUtils.commaDelimitedListToSet(v)));
            LOGGER.debug("Using accounts [{}] for surrogate authentication", accounts);
            return new SimpleSurrogateAuthenticationService(accounts);
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    public SurrogateAuthenticationAspect surrogateAuthenticationAspect() {
        return new SurrogateAuthenticationAspect(new DefaultPrincipalFactory(), surrogateAuthenticationService());
    }

    @Autowired
    @RefreshScope
    @Bean
    public PrincipalResolver personDirectoryPrincipalResolver(@Qualifier("attributeRepository") final IPersonAttributeDao attributeRepository,
                                                              @Qualifier("principalFactory") final PrincipalFactory principalFactory) {
        final SurrogatePrincipalResolver bean = new SurrogatePrincipalResolver();
        bean.setAttributeRepository(attributeRepository);
        bean.setPrincipalAttributeName(casProperties.getPersonDirectory().getPrincipalAttribute());
        bean.setReturnNullIfNoAttributes(casProperties.getPersonDirectory().isReturnNull());
        bean.setPrincipalFactory(principalFactory);
        return bean;
    }

    @Bean
    public PrincipalIdProvider principalIdProvider() {
        return new SurrogatePrincipalIdProvider();
    }
}
