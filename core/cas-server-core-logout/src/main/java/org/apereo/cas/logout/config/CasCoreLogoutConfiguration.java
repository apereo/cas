package org.apereo.cas.logout.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.DefaultLogoutExecutionPlan;
import org.apereo.cas.logout.DefaultLogoutManager;
import org.apereo.cas.logout.DefaultLogoutRedirectionStrategy;
import org.apereo.cas.logout.DefaultSingleLogoutMessageCreator;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.LogoutExecutionPlanConfigurer;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.LogoutRedirectionStrategy;
import org.apereo.cas.logout.slo.ChainingSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceMessageHandler;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilderConfigurer;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.UrlValidator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link CasCoreLogoutConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreLogoutConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreLogoutConfiguration {

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory> webApplicationServiceFactory;

    @Autowired
    @Qualifier("singleLogoutServiceLogoutUrlBuilder")
    private ObjectProvider<SingleLogoutServiceLogoutUrlBuilder> singleLogoutServiceLogoutUrlBuilder;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Autowired
    @Qualifier("urlValidator")
    private ObjectProvider<UrlValidator> urlValidator;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @ConditionalOnMissingBean(name = "singleLogoutServiceLogoutUrlBuilder")
    @Bean
    @RefreshScope
    public SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder() {
        val configurers = new ArrayList<>(
            applicationContext.getBeansOfType(SingleLogoutServiceLogoutUrlBuilderConfigurer.class, false, true).values());
        val results = configurers
            .stream()
            .sorted(Comparator.comparing(SingleLogoutServiceLogoutUrlBuilderConfigurer::getOrder))
            .map(cfg -> {
                val builder = cfg.configureBuilder();
                LOGGER.trace("Configuring single logout url builder [{}]", builder.getName());
                return builder;
            })
            .map(SingleLogoutServiceLogoutUrlBuilder.class::cast)
            .sorted(Comparator.comparing(SingleLogoutServiceLogoutUrlBuilder::getOrder))
            .collect(Collectors.toList());
        return new ChainingSingleLogoutServiceLogoutUrlBuilder(results);
    }

    @ConditionalOnMissingBean(name = "defaultSingleLogoutServiceLogoutUrlBuilderConfigurer")
    @Bean
    @RefreshScope
    public SingleLogoutServiceLogoutUrlBuilderConfigurer defaultSingleLogoutServiceLogoutUrlBuilderConfigurer() {
        return () -> new DefaultSingleLogoutServiceLogoutUrlBuilder(servicesManager.getObject(), urlValidator.getObject());
    }

    @ConditionalOnMissingBean(name = "defaultSingleLogoutServiceMessageHandler")
    @Bean
    @RefreshScope
    public SingleLogoutServiceMessageHandler defaultSingleLogoutServiceMessageHandler() {
        return new DefaultSingleLogoutServiceMessageHandler(httpClient.getObject(),
            defaultSingleLogoutMessageCreator(),
            servicesManager.getObject(),
            singleLogoutServiceLogoutUrlBuilder(),
            casProperties.getSlo().isAsynchronous(),
            authenticationServiceSelectionPlan.getObject());
    }

    @ConditionalOnMissingBean(name = "logoutManager")
    @RefreshScope
    @Autowired
    @Bean
    public LogoutManager logoutManager(@Qualifier("logoutExecutionPlan") final LogoutExecutionPlan logoutExecutionPlan) {
        return new DefaultLogoutManager(casProperties.getSlo().isDisabled(), logoutExecutionPlan);
    }

    @ConditionalOnMissingBean(name = "defaultSingleLogoutMessageCreator")
    @Bean
    public SingleLogoutMessageCreator defaultSingleLogoutMessageCreator() {
        return new DefaultSingleLogoutMessageCreator();
    }

    @ConditionalOnMissingBean(name = "logoutExecutionPlan")
    @Autowired
    @Bean
    @RefreshScope
    public LogoutExecutionPlan logoutExecutionPlan(final List<LogoutExecutionPlanConfigurer> configurers) {
        val plan = new DefaultLogoutExecutionPlan();
        configurers.forEach(c -> {
            LOGGER.trace("Configuring logout execution plan [{}]", c.getName());
            c.configureLogoutExecutionPlan(plan);
        });
        return plan;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultLogoutRedirectionStrategy")
    public LogoutRedirectionStrategy defaultLogoutRedirectionStrategy() {
        return new DefaultLogoutRedirectionStrategy(webApplicationServiceFactory.getObject(),
            casProperties.getLogout(), singleLogoutServiceLogoutUrlBuilder.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "casCoreLogoutExecutionPlanConfigurer")
    public LogoutExecutionPlanConfigurer casCoreLogoutExecutionPlanConfigurer() {
        return plan -> {
            plan.registerSingleLogoutServiceMessageHandler(defaultSingleLogoutServiceMessageHandler());
            plan.registerLogoutRedirectionStrategy(defaultLogoutRedirectionStrategy());

            if (casProperties.getLogout().isRemoveDescendantTickets()) {
                LOGGER.debug("CAS is configured to remove descendant tickets of the ticket-granting tickets");
                plan.registerLogoutPostProcessor(ticketGrantingTicket -> ticketGrantingTicket.getDescendantTickets()
                    .forEach(t -> {
                        LOGGER.debug("Deleting ticket [{}] from the registry as a descendant of [{}]", t, ticketGrantingTicket.getId());
                        ticketRegistry.getObject().deleteTicket(t);
                    }));
            }
        };
    }

}
