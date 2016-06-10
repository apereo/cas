package org.apereo.cas.adaptors.x509.config;

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
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link X509AuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("x509AuthenticationConfiguration")
public class X509AuthenticationConfiguration {

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
        final CRLDistributionPointRevocationChecker c = new CRLDistributionPointRevocationChecker();
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
        return new LdaptiveResourceCRLFetcher();
    }

    @Bean
    public CRLFetcher poolingLdaptiveResourceCRLFetcher() {
        return new PoolingLdaptiveResourceCRLFetcher();
    }

    @Bean
    public Action x509Check() {
        return new X509CertificateCredentialsNonInteractiveAction();
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectPrincipalResolver() {
        final X509SubjectPrincipalResolver p = new X509SubjectPrincipalResolver();
        p.setDescriptor(casProperties.getAuthn().getX509().getPrincipalDescriptor());
        return p;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectDNPrincipalResolver() {
        return new X509SubjectDNPrincipalResolver();
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectAlternativeNameUPNPrincipalResolver() {
        return new X509SubjectAlternativeNameUPNPrincipalResolver();
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SerialNumberPrincipalResolver() {
        return new X509SerialNumberPrincipalResolver();
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
