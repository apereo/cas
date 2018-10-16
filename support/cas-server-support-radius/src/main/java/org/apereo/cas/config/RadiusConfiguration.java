package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.radius.JRadiusServerImpl;
import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusProtocol;
import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.adaptors.radius.authentication.handler.support.RadiusAuthenticationHandler;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAccessChallengedAuthenticationWebflowEventResolver;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusClientProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusServerProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.authentication.RankedMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.util.CookieGenerator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This this {@link RadiusConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("radiusConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class RadiusConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("radiusPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration passwordPolicyConfiguration;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired(required = false)
    @Qualifier("multifactorAuthenticationProviderSelector")
    private MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector = new RankedMultifactorAuthenticationProviderSelector();

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CookieGenerator> warnCookieGenerator;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @ConditionalOnMissingBean(name = "radiusPrincipalFactory")
    @Bean
    public PrincipalFactory radiusPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public JRadiusServerImpl radiusServer() {
        final RadiusClientProperties client = casProperties.getAuthn().getRadius().getClient();
        final RadiusServerProperties server = casProperties.getAuthn().getRadius().getServer();

        final Set<String> ips = getClientIps(client);
        return getSingleRadiusServer(client, server, ips.iterator().next());
    }

    public static Set<String> getClientIps(final RadiusClientProperties client) {
        return StringUtils.commaDelimitedListToSet(StringUtils.trimAllWhitespace(client.getInetAddress()));
    }

    private static JRadiusServerImpl getSingleRadiusServer(final RadiusClientProperties client, final RadiusServerProperties server, final String clientInetAddress) {
        final RadiusClientFactory factory = new RadiusClientFactory(client.getAccountingPort(), client.getAuthenticationPort(), client.getSocketTimeout(),
            clientInetAddress, client.getSharedSecret());

        final RadiusProtocol protocol = RadiusProtocol.valueOf(server.getProtocol());

        return new JRadiusServerImpl(protocol, factory, server.getRetries(),
            server.getNasIpAddress(), server.getNasIpv6Address(), server.getNasPort(),
            server.getNasPortId(), server.getNasIdentifier(), server.getNasRealPort(),
            server.getNasPortType());
    }

    @RefreshScope
    @Bean
    public List<RadiusServer> radiusServers() {
        final RadiusClientProperties client = casProperties.getAuthn().getRadius().getClient();
        final RadiusServerProperties server = casProperties.getAuthn().getRadius().getServer();

        final Set<String> ips = getClientIps(casProperties.getAuthn().getRadius().getClient());
        return ips.stream().map(ip -> getSingleRadiusServer(client, server, ip)).collect(Collectors.toList());
    }

    @Bean
    public AuthenticationHandler radiusAuthenticationHandler() {
        final RadiusProperties radius = casProperties.getAuthn().getRadius();
        final RadiusAuthenticationHandler h = new RadiusAuthenticationHandler(radius.getName(), servicesManager, radiusPrincipalFactory(), radiusServers(),
            radius.isFailoverOnException(), radius.isFailoverOnAuthenticationFailure());

        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(radius.getPasswordEncoder()));
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(radius.getPrincipalTransformation()));

        if (passwordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(passwordPolicyConfiguration);
        }
        return h;
    }

    @RefreshScope
    @Bean
    public CasWebflowEventResolver radiusAccessChallengedAuthenticationWebflowEventResolver() {
        final CasWebflowEventResolver r = new RadiusAccessChallengedAuthenticationWebflowEventResolver(authenticationSystemSupport.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            servicesManager,
            ticketRegistrySupport.getIfAvailable(),
            warnCookieGenerator.getIfAvailable(),
            authenticationRequestServiceSelectionStrategies.getIfAvailable(),
            multifactorAuthenticationProviderSelector,
            casProperties.getAuthn().getMfa().getRadius().getId());


        LOGGER.debug("Activating MFA event resolver based on RADIUS...");
        this.initialAuthenticationAttemptWebflowEventResolver.getIfAvailable().addDelegate(r);
        return r;
    }

    @ConditionalOnMissingBean(name = "radiusAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer radiusAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            final Set<String> ips = getClientIps(casProperties.getAuthn().getRadius().getClient());
            if (!ips.isEmpty()) {
                plan.registerAuthenticationHandler(radiusAuthenticationHandler());
            } else {
                LOGGER.warn("No RADIUS address is defined. RADIUS support will be disabled.");
            }
        };
    }
}
