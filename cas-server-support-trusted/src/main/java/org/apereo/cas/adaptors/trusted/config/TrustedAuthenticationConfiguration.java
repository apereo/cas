package org.apereo.cas.adaptors.trusted.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.trusted.authentication.handler.support.PrincipalBearingCredentialsAuthenticationHandler;
import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingPrincipalResolver;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * This is {@link TrustedAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("trustedAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class TrustedAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Bean
    @RefreshScope
    public AuthenticationHandler principalBearingCredentialsAuthenticationHandler() {
        final PrincipalBearingCredentialsAuthenticationHandler h =
                new PrincipalBearingCredentialsAuthenticationHandler();
        h.setPrincipalFactory(trustedPrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver trustedPrincipalResolver() {
        final PrincipalBearingPrincipalResolver r = new PrincipalBearingPrincipalResolver();
        r.setAttributeRepository(this.attributeRepository);
        r.setPrincipalAttributeName(casProperties.getAuthn().getTrusted().getPrincipalAttribute());
        r.setReturnNullIfNoAttributes(casProperties.getAuthn().getTrusted().isReturnNull());
        r.setPrincipalFactory(trustedPrincipalFactory());
        return r;
    }

    @Bean
    public PrincipalFactory trustedPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public Action principalFromRemoteUserAction() {
        final PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction a =
                new PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction();

        a.setAuthenticationSystemSupport(authenticationSystemSupport);
        a.setCentralAuthenticationService(centralAuthenticationService);
        a.setPrincipalFactory(trustedPrincipalFactory());
        a.setWarnCookieGenerator(warnCookieGenerator);
        return a;
    }

    @Bean
    @RefreshScope
    public Action principalFromRemoteUserPrincipalAction() {
        final PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction a =
                new PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction();

        a.setAuthenticationSystemSupport(authenticationSystemSupport);
        a.setCentralAuthenticationService(centralAuthenticationService);
        a.setPrincipalFactory(trustedPrincipalFactory());
        a.setWarnCookieGenerator(warnCookieGenerator);
        return a;
    }

    @PostConstruct
    public void initializeAuthenticationHandler() {
        this.authenticationHandlersResolvers.put(principalBearingCredentialsAuthenticationHandler(),
                trustedPrincipalResolver());
    }
}
