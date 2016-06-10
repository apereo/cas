package org.apereo.cas.config;

import jcifs.spnego.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import java.util.Arrays;

/**
 * This is {@link SpnegoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("spnegoConfiguration")
public class SpnegoConfiguration {

    @Autowired(required = false)
    @Qualifier("spnegoClientActionConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired(required = false)
    @Qualifier("spnegoClientActionSearchRequest")
    private SearchRequest searchRequest;

    @Autowired(required = false)
    @Qualifier("spnegoPrincipalNameTransformer")
    private PrincipalNameTransformer transformer;

    @Autowired
    private CasConfigurationProperties casProperties;

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
        final JcifsConfig c = new JcifsConfig();

        
        c.setJcifsDomain(casProperties.getSpnegoProperties().getJcifsDomain());
        c.setJcifsDomainController(casProperties.getSpnegoProperties().getJcifsDomainController());
        c.setJcifsNetbiosCachePolicy(casProperties.getSpnegoProperties().getCachePolicy());
        c.setJcifsNetbiosWins(casProperties.getSpnegoProperties().getJcifsNetbiosWins());
        c.setJcifsPassword(casProperties.getSpnegoProperties().getJcifsPassword());
        c.setJcifsServicePassword(casProperties.getSpnegoProperties().getJcifsServicePassword());
        c.setJcifsServicePrincipal(casProperties.getSpnegoProperties().getJcifsServicePrincipal());
        c.setJcifsSocketTimeout(casProperties.getSpnegoProperties().getTimeout());
        c.setJcifsUsername(casProperties.getSpnegoProperties().getJcifsUsername());
        c.setKerberosConf(casProperties.getSpnegoProperties().getKerberosConf());
        c.setKerberosDebug(casProperties.getSpnegoProperties().getKerberosDebug());
        c.setKerberosKdc(casProperties.getSpnegoProperties().getKerberosKdc());
        c.setKerberosRealm(casProperties.getSpnegoProperties().getKerberosRealm());
        c.setLoginConf(casProperties.getSpnegoProperties().getLoginConf());
        c.setUseSubjectCredsOnly(casProperties.getSpnegoProperties().isUseSubjectCredsOnly());

        
        return c;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler spnegoHandler() {
        final JcifsSpnegoAuthenticationHandler h = new JcifsSpnegoAuthenticationHandler();

        h.setAuthentication(spnegoAuthentication());
        h.setPrincipalWithDomainName(casProperties.getSpnegoProperties().isPrincipalWithDomainName());
        h.setNTLMallowed(casProperties.getSpnegoProperties().isNtlmAllowed());
        return h;
    }


    @Bean
    @RefreshScope
    public AuthenticationHandler ntlmAuthenticationHandler() {
        final NtlmAuthenticationHandler ntlm = new NtlmAuthenticationHandler();
        ntlm.setDomainController(casProperties.getNtlmProperties().getDomainController());
        ntlm.setIncludePattern(casProperties.getNtlmProperties().getIncludePattern());
        ntlm.setLoadBalance(casProperties.getNtlmProperties().isLoadBalance());
        return ntlm;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver spnegoPrincipalResolver() {
        final SpnegoPrincipalResolver r = new SpnegoPrincipalResolver();
        if (transformer != null) {
            r.setPrincipalNameTransformer(transformer);
        }
        return r;
    }

    @Bean
    @RefreshScope
    public SpnegoCredentialsAction spnego() {
        final SpnegoCredentialsAction a = new SpnegoCredentialsAction();
        a.setNtlm(casProperties.getSpnegoProperties().isNtlm());
        a.setSend401OnAuthenticationFailure(casProperties.getSpnegoProperties().isSend401OnAuthenticationFailure());
        return a;
    }

    @Bean
    @RefreshScope
    public Action negociateSpnego() {
        final SpnegoNegociateCredentialsAction a =
                new SpnegoNegociateCredentialsAction();
        a.setMixedModeAuthentication(casProperties.getSpnegoProperties().isMixedModeAuthentication());
        a.setNtlm(casProperties.getSpnegoProperties().isNtlm());
        a.setSupportedBrowsers(Arrays.asList(casProperties.getSpnegoProperties().getSupportedBrowsers()));
        return a;
    }

    @Bean
    @RefreshScope
    public Action baseSpnegoClientAction() {
        final BaseSpnegoKnownClientSystemsFilterAction a =
                new BaseSpnegoKnownClientSystemsFilterAction();

        a.setIpsToCheckPattern(casProperties.getSpnegoProperties().getIpsToCheckPattern());
        a.setAlternativeRemoteHostAttribute(casProperties.getSpnegoProperties().getAlternativeRemoteHostAttribute());
        a.setTimeout(casProperties.getSpnegoProperties().getDnsTimeout());
        return a;
    }

    @Bean
    @RefreshScope
    public Action hostnameSpnegoClientAction() {
        final HostNameSpnegoKnownClientSystemsFilterAction a =
                new HostNameSpnegoKnownClientSystemsFilterAction(casProperties.getSpnegoProperties().getHostNamePatternString());
        a.setIpsToCheckPattern(casProperties.getSpnegoProperties().getIpsToCheckPattern());
        a.setAlternativeRemoteHostAttribute(casProperties.getSpnegoProperties().getAlternativeRemoteHostAttribute());
        a.setTimeout(casProperties.getSpnegoProperties().getDnsTimeout());
        return a;
    }

    @Bean
    @RefreshScope
    public Action ldapSpnegoClientAction() {
        final LdapSpnegoKnownClientSystemsFilterAction l =
                new LdapSpnegoKnownClientSystemsFilterAction(this.connectionFactory,
                        this.searchRequest, casProperties.getSpnegoProperties().getSpnegoAttributeName());

        l.setIpsToCheckPattern(casProperties.getSpnegoProperties().getIpsToCheckPattern());
        l.setAlternativeRemoteHostAttribute(casProperties.getSpnegoProperties().getAlternativeRemoteHostAttribute());
        l.setTimeout(casProperties.getSpnegoProperties().getDnsTimeout());
        return l;
    }
}
