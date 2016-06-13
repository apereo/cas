package org.apereo.cas.adaptors.x509.config;

import net.sf.ehcache.Cache;
import org.apereo.cas.adaptors.x509.authentication.handler.support.AllowRevocationPolicy;
import org.apereo.cas.adaptors.x509.authentication.handler.support.CRLDistributionPointRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.handler.support.CRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.handler.support.DenyRevocationPolicy;
import org.apereo.cas.adaptors.x509.authentication.handler.support.NoOpRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.handler.support.ResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.handler.support.ResourceCRLRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.handler.support.RevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.handler.support.RevocationPolicy;
import org.apereo.cas.adaptors.x509.authentication.handler.support.ThresholdExpiredCRLRevocationPolicy;
import org.apereo.cas.adaptors.x509.authentication.handler.support.X509CredentialsAuthenticationHandler;
import org.apereo.cas.adaptors.x509.authentication.handler.support.ldap.LdaptiveResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.handler.support.ldap.PoolingLdaptiveResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SerialNumberAndIssuerDNPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SerialNumberPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SubjectAlternativeNameUPNPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SubjectDNPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SubjectPrincipalResolver;
import org.apereo.cas.adaptors.x509.web.flow.X509CertificateCredentialsNonInteractiveAction;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.SearchExecutor;
import org.ldaptive.pool.BlockingConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import java.util.Set;

/**
 * This is {@link X509AuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("x509AuthenticationConfiguration")
public class X509AuthenticationConfiguration {

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;
    
    @Autowired(required = false)
    @Qualifier("poolingLdaptiveResourceCRLConnectionConfig")
    private ConnectionConfig poolingLdaptiveResourceCRLConnectionConfig;

    @Autowired(required = false)
    @Qualifier("poolingLdaptiveConnectionPool")
    private BlockingConnectionPool poolingLdaptiveConnectionPool;

    @Autowired(required = false)
    @Qualifier("poolingLdaptiveResourceCRLSearchExecutor")
    private SearchExecutor poolingLdaptiveResourceCRLSearchExecutor;

    @Autowired(required = false)
    @Qualifier("x509ResourceUnavailableRevocationPolicy")
    private RevocationPolicy x509ResourceUnavailableRevocationPolicy;

    @Autowired(required = false)
    @Qualifier("x509ResourceExpiredRevocationPolicy")
    private RevocationPolicy x509ResourceExpiredRevocationPolicy;

    @Autowired(required = false)
    @Qualifier("x509CrlUnavailableRevocationPolicy")
    private RevocationPolicy x509CrlUnavailableRevocationPolicy;

    @Autowired(required = false)
    @Qualifier("x509CrlExpiredRevocationPolicy")
    private RevocationPolicy x509CrlExpiredRevocationPolicy;

    @Autowired(required = false)
    @Qualifier("x509RevocationChecker")
    private RevocationChecker revocationChecker;

    @Autowired(required = false)
    @Qualifier("x509CrlResources")
    private Set x509CrlResources;

    @Autowired(required = false)
    @Qualifier("x509CrlFetcher")
    private CRLFetcher x509CrlFetcher;

    @Autowired(required = false)
    @Qualifier("x509CrlCache")
    private Cache x509CrlCache;

    @Autowired(required = false)
    @Qualifier("ldaptiveResourceCRLSearchExecutor")
    private SearchExecutor ldaptiveResourceCRLSearchExecutor;

    @Autowired(required = false)
    @Qualifier("ldaptiveResourceCRLConnectionConfig")
    private ConnectionConfig ldaptiveResourceCRLConnectionConfig;
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public RevocationPolicy allowRevocationPolicy() {
        return new AllowRevocationPolicy();
    }

    @Bean
    @RefreshScope
    public RevocationPolicy thresholdExpiredCRLRevocationPolicy() {
        final ThresholdExpiredCRLRevocationPolicy p = new ThresholdExpiredCRLRevocationPolicy();
        p.setThreshold(casProperties.getAuthn().getX509().getRevocationPolicyThreshold());
        return p;
    }

    @Bean
    public RevocationPolicy denyRevocationPolicy() {
        return new DenyRevocationPolicy();
    }

    @Bean
    public RevocationChecker crlDistributionPointRevocationChecker() {
        final CRLDistributionPointRevocationChecker c =
                new CRLDistributionPointRevocationChecker(this.x509CrlCache, this.x509CrlFetcher);
        c.setCheckAll(casProperties.getAuthn().getX509().isCheckAll());
        c.setThrowOnFetchFailure(casProperties.getAuthn().getX509().isThrowOnFetchFailure());
        c.setUnavailableCRLPolicy(x509CrlUnavailableRevocationPolicy);
        c.setExpiredCRLPolicy(x509CrlExpiredRevocationPolicy);
        return c;
    }

    @Bean
    public RevocationChecker noOpRevocationChecker() {
        return new NoOpRevocationChecker();
    }

    @Bean
    public CRLFetcher resourceCrlFetcher() {
        return new ResourceCRLFetcher();
    }

    @Bean
    public RevocationChecker resourceCrlRevocationChecker() {
        final ResourceCRLRevocationChecker c = new ResourceCRLRevocationChecker();

        c.setRefreshInterval(casProperties.getAuthn().getX509().getRefreshIntervalSeconds());
        c.setCheckAll(casProperties.getAuthn().getX509().isCheckAll());
        c.setExpiredCRLPolicy(this.x509ResourceExpiredRevocationPolicy);
        c.setUnavailableCRLPolicy(this.x509ResourceUnavailableRevocationPolicy);
        c.setResources(x509CrlResources);
        c.setFetcher(this.x509CrlFetcher);

        return c;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler x509CredentialsAuthenticationHandler() {
        final X509CredentialsAuthenticationHandler h = new X509CredentialsAuthenticationHandler();

        h.setCheckKeyUsage(casProperties.getAuthn().getX509().isCheckKeyUsage());
        h.setMaxPathLength(casProperties.getAuthn().getX509().getMaxPathLength());
        h.setMaxPathLengthAllowUnspecified(casProperties.getAuthn().getX509().isMaxPathLengthAllowUnspecified());
        h.setRequireKeyUsage(casProperties.getAuthn().getX509().isRequireKeyUsage());
        h.setRevocationChecker(this.revocationChecker);
        h.setSubjectDnPattern(casProperties.getAuthn().getX509().getRegExSubjectDnPattern());
        h.setTrustedIssuerDnPattern(casProperties.getAuthn().getX509().getRegExTrustedIssuerDnPattern());

        return h;
    }

    @Bean
    public CRLFetcher ldaptiveResourceCRLFetcher() {
        final LdaptiveResourceCRLFetcher r = new LdaptiveResourceCRLFetcher();
        r.setConnectionConfig(this.ldaptiveResourceCRLConnectionConfig);
        r.setSearchExecutor(this.ldaptiveResourceCRLSearchExecutor);
        return r;
    }

    @Bean
    public CRLFetcher poolingLdaptiveResourceCRLFetcher() {
        final PoolingLdaptiveResourceCRLFetcher f = new PoolingLdaptiveResourceCRLFetcher();

        f.setSearchExecutor(this.poolingLdaptiveResourceCRLSearchExecutor);
        f.setConnectionPool(this.poolingLdaptiveConnectionPool);
        f.setConnectionConfig(this.poolingLdaptiveResourceCRLConnectionConfig);
        return f;
    }

    @Bean
    public Action x509Check() {
        return new X509CertificateCredentialsNonInteractiveAction();
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectPrincipalResolver() {
        final X509SubjectPrincipalResolver r = new X509SubjectPrincipalResolver();
        r.setDescriptor(casProperties.getAuthn().getX509().getPrincipalDescriptor());
        r.setAttributeRepository(attributeRepository);
        r.setPrincipalAttributeName(casProperties.getAuthn().getX509().getPrincipal().getPrincipalAttribute());
        r.setReturnNullIfNoAttributes(casProperties.getAuthn().getX509().getPrincipal().isReturnNull());
        r.setPrincipalFactory(x509PrincipalFactory());
        return r;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectDNPrincipalResolver() {
        final X509SubjectDNPrincipalResolver r = new X509SubjectDNPrincipalResolver();
        r.setAttributeRepository(attributeRepository);
        r.setPrincipalAttributeName(casProperties.getAuthn().getX509().getPrincipal().getPrincipalAttribute());
        r.setReturnNullIfNoAttributes(casProperties.getAuthn().getX509().getPrincipal().isReturnNull());
        r.setPrincipalFactory(x509PrincipalFactory());
        return r;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectAlternativeNameUPNPrincipalResolver() {
        final X509SubjectAlternativeNameUPNPrincipalResolver r =
                new X509SubjectAlternativeNameUPNPrincipalResolver();
        r.setAttributeRepository(attributeRepository);
        r.setPrincipalAttributeName(casProperties.getAuthn().getX509().getPrincipal().getPrincipalAttribute());
        r.setReturnNullIfNoAttributes(casProperties.getAuthn().getX509().getPrincipal().isReturnNull());
        r.setPrincipalFactory(x509PrincipalFactory());
        return r;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SerialNumberPrincipalResolver() {
        final X509SerialNumberPrincipalResolver r = new X509SerialNumberPrincipalResolver();
        r.setAttributeRepository(attributeRepository);
        r.setPrincipalAttributeName(casProperties.getAuthn().getX509().getPrincipal().getPrincipalAttribute());
        r.setReturnNullIfNoAttributes(casProperties.getAuthn().getX509().getPrincipal().isReturnNull());
        r.setPrincipalFactory(x509PrincipalFactory());
        return r;
    }

    @Bean
    public PrincipalFactory x509PrincipalFactory() {
        return new DefaultPrincipalFactory();
    }



    
    @Bean
    @RefreshScope
    public PrincipalResolver x509SerialNumberAndIssuerDNPrincipalResolver() {
        final X509SerialNumberAndIssuerDNPrincipalResolver r =
                new X509SerialNumberAndIssuerDNPrincipalResolver();

        r.setSerialNumberPrefix(casProperties.getAuthn().getX509().getSerialNumberPrefix());
        r.setValueDelimiter(casProperties.getAuthn().getX509().getValueDelimiter());

        return r;
    }
}
