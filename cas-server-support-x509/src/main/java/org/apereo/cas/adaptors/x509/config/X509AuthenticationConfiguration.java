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
    
    @Bean
    public RevocationPolicy allowRevocationPolicy() {
        return new AllowRevocationPolicy();
    }

    @Bean
    @RefreshScope
    public RevocationPolicy thresholdExpiredCRLRevocationPolicy() {
        return new ThresholdExpiredCRLRevocationPolicy();
    }
    
    @Bean
    public RevocationPolicy denyRevocationPolicy() {
        return new DenyRevocationPolicy();
    }
    
    @Bean
    public RevocationChecker crlDistributionPointRevocationChecker() {
        return new CRLDistributionPointRevocationChecker();
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
        return new ResourceCRLRevocationChecker();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler x509CredentialsAuthenticationHandler() {
        return new X509CredentialsAuthenticationHandler();
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
        return new X509SubjectPrincipalResolver();
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
        return new X509SerialNumberAndIssuerDNPrincipalResolver();
    }
}
