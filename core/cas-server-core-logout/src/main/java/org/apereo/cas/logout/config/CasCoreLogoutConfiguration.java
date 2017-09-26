package org.apereo.cas.logout.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.DefaultLogoutExecutionPlan;
import org.apereo.cas.logout.DefaultLogoutManager;
import org.apereo.cas.logout.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.DefaultSingleLogoutServiceMessageHandler;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.LogoutExecutionPlanConfigurer;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.LogoutMessageCreator;
import org.apereo.cas.logout.SamlCompliantLogoutMessageCreator;
import org.apereo.cas.logout.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.SingleLogoutServiceMessageHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class CasCoreLogoutConfiguration implements LogoutExecutionPlanConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreLogoutConfiguration.class);

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    private UrlValidator urlValidator;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @ConditionalOnMissingBean(name = "singleLogoutServiceLogoutUrlBuilder")
    @Bean
    public SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder() {
        return new DefaultSingleLogoutServiceLogoutUrlBuilder(this.urlValidator);
    }

    @ConditionalOnMissingBean(name = "defaultSingleLogoutServiceMessageHandler")
    @Bean
    public SingleLogoutServiceMessageHandler defaultSingleLogoutServiceMessageHandler() {
        return new DefaultSingleLogoutServiceMessageHandler(httpClient,
                logoutBuilder(),
                servicesManager,
                singleLogoutServiceLogoutUrlBuilder(),
                casProperties.getSlo().isAsynchronous(),
                authenticationRequestServiceSelectionStrategies);
    }

    @ConditionalOnMissingBean(name = "logoutManager")
    @RefreshScope
    @Autowired
    @Bean
    public LogoutManager logoutManager(@Qualifier("logoutExecutionPlan") final LogoutExecutionPlan logoutExecutionPlan) {
        return new DefaultLogoutManager(logoutBuilder(), defaultSingleLogoutServiceMessageHandler(),
                casProperties.getSlo().isDisabled(), logoutExecutionPlan);
    }

    @ConditionalOnMissingBean(name = "logoutBuilder")
    @Bean
    public LogoutMessageCreator logoutBuilder() {
        return new SamlCompliantLogoutMessageCreator();
    }

    @ConditionalOnMissingBean(name = "logoutExecutionPlan")
    @Autowired
    @Bean
    public LogoutExecutionPlan logoutExecutionPlan(final List<LogoutExecutionPlanConfigurer> configurers) {
        final DefaultLogoutExecutionPlan plan = new DefaultLogoutExecutionPlan();
        configurers.forEach(c -> {
            final String name = StringUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Configuring logout execution plan [{}]", name);
            c.configureLogoutExecutionPlan(plan);
        });
        return plan;
    }

    @Override
    public void configureLogoutExecutionPlan(final LogoutExecutionPlan plan) {
        if (casProperties.getLogout().isRemoveDescendantTickets()) {
            LOGGER.debug("CAS is configured to remove descendant tickets of the ticket-granting tickets");
            plan.registerLogoutHandler(ticketGrantingTicket -> ticketGrantingTicket.getDescendantTickets()
                    .stream()
                    .forEach(t -> {
                        LOGGER.debug("Deleting ticket [{}] from the registry as a descendant of [{}]", t, ticketGrantingTicket.getId());
                        ticketRegistry.deleteTicket(t);
                    }));
        }
    }
}
