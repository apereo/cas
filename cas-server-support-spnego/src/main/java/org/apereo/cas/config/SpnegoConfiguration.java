package org.apereo.cas.config;

import jcifs.spnego.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.model.support.ntlm.NtlmProperties;
import org.apereo.cas.support.spnego.SpnegoApplicationContextWrapper;
import org.apereo.cas.support.spnego.authentication.handler.support.JcifsConfig;
import org.apereo.cas.support.spnego.authentication.handler.support.JcifsSpnegoAuthenticationHandler;
import org.apereo.cas.support.spnego.authentication.handler.support.NtlmAuthenticationHandler;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoPrincipalResolver;
import org.apereo.cas.support.spnego.web.flow.SpnegoCredentialsAction;
import org.apereo.cas.support.spnego.web.flow.SpnegoNegociateCredentialsAction;
import org.apereo.cas.support.spnego.web.flow.client.BaseSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.support.spnego.web.flow.client.HostNameSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.support.spnego.web.flow.client.LdapSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link SpnegoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("spnegoConfiguration")
public class SpnegoConfiguration {

    @Autowired
    private NtlmProperties ntlmProperties;

    @RefreshScope
    @Bean
    public Authentication spnegoAuthentication() {
        return new Authentication();
    }

    @Bean
    public BaseApplicationContextWrapper spnegoApplicationContextWrapper() {
        return new SpnegoApplicationContextWrapper();
    }

    @Bean
    @RefreshScope
    public JcifsConfig jcifsConfig() {
        return new JcifsConfig();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler spnegoHandler() {
        return new JcifsSpnegoAuthenticationHandler();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler ntlmAuthenticationHandler() {
        final NtlmAuthenticationHandler ntlm = new NtlmAuthenticationHandler();
        ntlm.setDomainController(ntlmProperties.getDomainController());
        ntlm.setIncludePattern(ntlmProperties.getIncludePattern());
        ntlm.setLoadBalance(ntlmProperties.isLoadBalance());
        return ntlm;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver spnegoPrincipalResolver() {
        return new SpnegoPrincipalResolver();
    }

    @Bean
    @RefreshScope
    public Action spnego() {
        return new SpnegoCredentialsAction();
    }

    @Bean
    @RefreshScope
    public Action negociateSpnego() {
        return new SpnegoNegociateCredentialsAction();
    }

    @Bean
    @RefreshScope
    public Action baseSpnegoClientAction() {
        return new BaseSpnegoKnownClientSystemsFilterAction();
    }

    @Bean
    @RefreshScope
    public Action hostnameSpnegoClientAction() {
        return new HostNameSpnegoKnownClientSystemsFilterAction();
    }

    @Bean
    @RefreshScope
    public Action ldapSpnegoClientAction() {
        return new LdapSpnegoKnownClientSystemsFilterAction();
    }
}
