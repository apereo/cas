package org.apereo.cas.config;

import jcifs.spnego.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.model.support.ntlm.NtlmProperties;
import org.apereo.cas.configuration.model.support.spnego.SpnegoProperties;
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
    private NtlmProperties ntlmProperties;

    @Autowired
    private SpnegoProperties spnegoProperties;

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

        c.setJcifsDomain(spnegoProperties.getJcifsDomain());
        c.setJcifsDomainController(spnegoProperties.getJcifsDomainController());
        c.setJcifsNetbiosCachePolicy(spnegoProperties.getCachePolicy());
        c.setJcifsNetbiosWins(spnegoProperties.getJcifsNetbiosWins());
        c.setJcifsPassword(spnegoProperties.getJcifsPassword());
        c.setJcifsServicePassword(spnegoProperties.getJcifsServicePassword());
        c.setJcifsServicePrincipal(spnegoProperties.getJcifsServicePrincipal());
        c.setJcifsSocketTimeout(spnegoProperties.getTimeout());
        c.setJcifsUsername(spnegoProperties.getJcifsUsername());
        c.setKerberosConf(spnegoProperties.getKerberosConf());
        c.setKerberosDebug(spnegoProperties.getKerberosDebug());
        c.setKerberosKdc(spnegoProperties.getKerberosKdc());
        c.setKerberosRealm(spnegoProperties.getKerberosRealm());
        c.setLoginConf(spnegoProperties.getLoginConf());
        c.setUseSubjectCredsOnly(spnegoProperties.isUseSubjectCredsOnly());

        return c;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler spnegoHandler() {
        final JcifsSpnegoAuthenticationHandler h = new JcifsSpnegoAuthenticationHandler();

        h.setAuthentication(spnegoAuthentication());
        h.setPrincipalWithDomainName(spnegoProperties.isPrincipalWithDomainName());
        h.setNTLMallowed(spnegoProperties.isNtlmAllowed());
        return h;
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
        a.setNtlm(spnegoProperties.isNtlm());
        a.setSend401OnAuthenticationFailure(spnegoProperties.isSend401OnAuthenticationFailure());
        return a;
    }

    @Bean
    @RefreshScope
    public Action negociateSpnego() {
        final SpnegoNegociateCredentialsAction a =
                new SpnegoNegociateCredentialsAction();
        a.setMixedModeAuthentication(spnegoProperties.isMixedModeAuthentication());
        a.setNtlm(spnegoProperties.isNtlm());
        a.setSupportedBrowsers(Arrays.asList(spnegoProperties.getSupportedBrowsers()));
        return a;
    }

    @Bean
    @RefreshScope
    public Action baseSpnegoClientAction() {
        final BaseSpnegoKnownClientSystemsFilterAction a =
                new BaseSpnegoKnownClientSystemsFilterAction();

        a.setIpsToCheckPattern(spnegoProperties.getIpsToCheckPattern());
        a.setAlternativeRemoteHostAttribute(spnegoProperties.getAlternativeRemoteHostAttribute());
        a.setTimeout(spnegoProperties.getDnsTimeout());
        return a;
    }

    @Bean
    @RefreshScope
    public Action hostnameSpnegoClientAction() {
        final HostNameSpnegoKnownClientSystemsFilterAction a =
                new HostNameSpnegoKnownClientSystemsFilterAction(spnegoProperties.getHostNamePatternString());
        a.setIpsToCheckPattern(spnegoProperties.getIpsToCheckPattern());
        a.setAlternativeRemoteHostAttribute(spnegoProperties.getAlternativeRemoteHostAttribute());
        a.setTimeout(spnegoProperties.getDnsTimeout());
        return a;
    }

    @Bean
    @RefreshScope
    public Action ldapSpnegoClientAction() {
        final LdapSpnegoKnownClientSystemsFilterAction l =
                new LdapSpnegoKnownClientSystemsFilterAction(this.connectionFactory,
                        this.searchRequest, spnegoProperties.getSpnegoAttributeName());

        l.setIpsToCheckPattern(spnegoProperties.getIpsToCheckPattern());
        l.setAlternativeRemoteHostAttribute(spnegoProperties.getAlternativeRemoteHostAttribute());
        l.setTimeout(spnegoProperties.getDnsTimeout());
        return l;
    }
}
