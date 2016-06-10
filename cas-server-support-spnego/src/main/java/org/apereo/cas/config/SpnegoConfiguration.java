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

        
        c.setJcifsDomain(casProperties.getSpnego().getJcifsDomain());
        c.setJcifsDomainController(casProperties.getSpnego().getJcifsDomainController());
        c.setJcifsNetbiosCachePolicy(casProperties.getSpnego().getCachePolicy());
        c.setJcifsNetbiosWins(casProperties.getSpnego().getJcifsNetbiosWins());
        c.setJcifsPassword(casProperties.getSpnego().getJcifsPassword());
        c.setJcifsServicePassword(casProperties.getSpnego().getJcifsServicePassword());
        c.setJcifsServicePrincipal(casProperties.getSpnego().getJcifsServicePrincipal());
        c.setJcifsSocketTimeout(casProperties.getSpnego().getTimeout());
        c.setJcifsUsername(casProperties.getSpnego().getJcifsUsername());
        c.setKerberosConf(casProperties.getSpnego().getKerberosConf());
        c.setKerberosDebug(casProperties.getSpnego().getKerberosDebug());
        c.setKerberosKdc(casProperties.getSpnego().getKerberosKdc());
        c.setKerberosRealm(casProperties.getSpnego().getKerberosRealm());
        c.setLoginConf(casProperties.getSpnego().getLoginConf());
        c.setUseSubjectCredsOnly(casProperties.getSpnego().isUseSubjectCredsOnly());

        
        return c;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler spnegoHandler() {
        final JcifsSpnegoAuthenticationHandler h = new JcifsSpnegoAuthenticationHandler();

        h.setAuthentication(spnegoAuthentication());
        h.setPrincipalWithDomainName(casProperties.getSpnego().isPrincipalWithDomainName());
        h.setNTLMallowed(casProperties.getSpnego().isNtlmAllowed());
        return h;
    }


    @Bean
    @RefreshScope
    public AuthenticationHandler ntlmAuthenticationHandler() {
        final NtlmAuthenticationHandler ntlm = new NtlmAuthenticationHandler();
        ntlm.setDomainController(casProperties.getNtlm().getDomainController());
        ntlm.setIncludePattern(casProperties.getNtlm().getIncludePattern());
        ntlm.setLoadBalance(casProperties.getNtlm().isLoadBalance());
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
        a.setNtlm(casProperties.getSpnego().isNtlm());
        a.setSend401OnAuthenticationFailure(casProperties.getSpnego().isSend401OnAuthenticationFailure());
        return a;
    }

    @Bean
    @RefreshScope
    public Action negociateSpnego() {
        final SpnegoNegociateCredentialsAction a =
                new SpnegoNegociateCredentialsAction();
        a.setMixedModeAuthentication(casProperties.getSpnego().isMixedModeAuthentication());
        a.setNtlm(casProperties.getSpnego().isNtlm());
        a.setSupportedBrowsers(Arrays.asList(casProperties.getSpnego().getSupportedBrowsers()));
        return a;
    }

    @Bean
    @RefreshScope
    public Action baseSpnegoClientAction() {
        final BaseSpnegoKnownClientSystemsFilterAction a =
                new BaseSpnegoKnownClientSystemsFilterAction();

        a.setIpsToCheckPattern(casProperties.getSpnego().getIpsToCheckPattern());
        a.setAlternativeRemoteHostAttribute(casProperties.getSpnego().getAlternativeRemoteHostAttribute());
        a.setTimeout(casProperties.getSpnego().getDnsTimeout());
        return a;
    }

    @Bean
    @RefreshScope
    public Action hostnameSpnegoClientAction() {
        final HostNameSpnegoKnownClientSystemsFilterAction a =
                new HostNameSpnegoKnownClientSystemsFilterAction(casProperties.getSpnego().getHostNamePatternString());
        a.setIpsToCheckPattern(casProperties.getSpnego().getIpsToCheckPattern());
        a.setAlternativeRemoteHostAttribute(casProperties.getSpnego().getAlternativeRemoteHostAttribute());
        a.setTimeout(casProperties.getSpnego().getDnsTimeout());
        return a;
    }

    @Bean
    @RefreshScope
    public Action ldapSpnegoClientAction() {
        final LdapSpnegoKnownClientSystemsFilterAction l =
                new LdapSpnegoKnownClientSystemsFilterAction(this.connectionFactory,
                        this.searchRequest, casProperties.getSpnego().getSpnegoAttributeName());

        l.setIpsToCheckPattern(casProperties.getSpnego().getIpsToCheckPattern());
        l.setAlternativeRemoteHostAttribute(casProperties.getSpnego().getAlternativeRemoteHostAttribute());
        l.setTimeout(casProperties.getSpnego().getDnsTimeout());
        return l;
    }
}
