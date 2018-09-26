package org.apereo.cas.logout.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.DefaultLogoutExecutionPlan;
import org.apereo.cas.logout.DefaultLogoutManager;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.LogoutExecutionPlanConfigurer;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.LogoutMessageCreator;
import org.apereo.cas.logout.SamlCompliantLogoutMessageCreator;
import org.apereo.cas.logout.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.SingleLogoutServiceMessageHandler;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceMessageHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.UrlValidator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * This is {@link CasCoreLogoutConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreLogoutConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreLogoutConfiguration implements LogoutExecutionPlanConfigurer {

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Autowired
    private ObjectProvider<UrlValidator> urlValidator;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;

    @ConditionalOnMissingBean(name = "singleLogoutServiceLogoutUrlBuilder")
    @Bean
    public SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder() {
        return new DefaultSingleLogoutServiceLogoutUrlBuilder(this.urlValidator.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "defaultSingleLogoutServiceMessageHandler")
    @Bean
    public SingleLogoutServiceMessageHandler defaultSingleLogoutServiceMessageHandler() {
        return new DefaultSingleLogoutServiceMessageHandler(httpClient.getIfAvailable(),
            defaultLogoutBuilder(),
            servicesManager.getIfAvailable(),
            singleLogoutServiceLogoutUrlBuilder(),
            casProperties.getSlo().isAsynchronous(),
            authenticationServiceSelectionPlan.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "logoutManager")
    @RefreshScope
    @Autowired
    @Bean
    public LogoutManager logoutManager(@Qualifier("logoutExecutionPlan") final LogoutExecutionPlan logoutExecutionPlan) {
        return new DefaultLogoutManager(defaultLogoutBuilder(), casProperties.getSlo().isDisabled(), logoutExecutionPlan);
    }

    @ConditionalOnMissingBean(name = "defaultLogoutBuilder")
    @Bean
    public LogoutMessageCreator defaultLogoutBuilder() {
        return new SamlCompliantLogoutMessageCreator();
    }

    @ConditionalOnMissingBean(name = "logoutExecutionPlan")
    @Autowired
    @Bean
    public LogoutExecutionPlan logoutExecutionPlan(final List<LogoutExecutionPlanConfigurer> configurers) {
        val plan = new DefaultLogoutExecutionPlan();
        configurers.forEach(c -> {
            val name = RegExUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Configuring logout execution plan [{}]", name);
            c.configureLogoutExecutionPlan(plan);
        });
        return plan;
    }

    @Override
    public void configureLogoutExecutionPlan(final LogoutExecutionPlan plan) {
        plan.registerSingleLogoutServiceMessageHandler(defaultSingleLogoutServiceMessageHandler());

        if (casProperties.getLogout().isRemoveDescendantTickets()) {
            LOGGER.debug("CAS is configured to remove descendant tickets of the ticket-granting tickets");
            plan.registerLogoutHandler(ticketGrantingTicket -> ticketGrantingTicket.getDescendantTickets()
                .forEach(t -> {
                    LOGGER.debug("Deleting ticket [{}] from the registry as a descendant of [{}]", t, ticketGrantingTicket.getId());
                    ticketRegistry.getObject().deleteTicket(t);
                }));
        }
    }
}
