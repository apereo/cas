package org.apereo.cas.adaptors.x509.config;

import com.google.common.collect.Sets;
import net.sf.ehcache.Cache;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.x509.authentication.CRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.ResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.handler.support.X509CredentialsAuthenticationHandler;
import org.apereo.cas.adaptors.x509.authentication.ldap.LdaptiveResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SerialNumberAndIssuerDNPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SerialNumberPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SubjectAlternativeNameUPNPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SubjectDNPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SubjectPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.CRLDistributionPointRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.NoOpRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.ResourceCRLRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.RevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.AllowRevocationPolicy;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.DenyRevocationPolicy;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.RevocationPolicy;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.ThresholdExpiredCRLRevocationPolicy;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.x509.X509Properties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This is {@link X509AuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("x509AuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class X509AuthenticationConfiguration {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    
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
        final X509Properties x509 = casProperties.getAuthn().getX509();
        final Cache cache = new Cache("CRL".concat(UUID.randomUUID().toString()),
                x509.getCacheMaxElementsInMemory(), x509.isCacheDiskOverflow(),
                x509.isCacheEternal(), x509.getCacheTimeToLiveSeconds(), x509.getCacheTimeToIdleSeconds());

        final CRLDistributionPointRevocationChecker c = new CRLDistributionPointRevocationChecker(cache, getCrlFetcher());
        c.setCheckAll(casProperties.getAuthn().getX509().isCheckAll());
        c.setThrowOnFetchFailure(casProperties.getAuthn().getX509().isThrowOnFetchFailure());
        c.setExpiredCRLPolicy(getRevocationPolicy(x509.getCrlExpiredPolicy()));
        c.setUnavailableCRLPolicy(getRevocationPolicy(x509.getCrlUnavailablePolicy()));
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
        final X509Properties x509 = casProperties.getAuthn().getX509();
        final ResourceCRLRevocationChecker c = new ResourceCRLRevocationChecker();

        c.setRefreshInterval(x509.getRefreshIntervalSeconds());
        c.setCheckAll(x509.isCheckAll());
        c.setExpiredCRLPolicy(getRevocationPolicy(x509.getCrlResourceExpiredPolicy()));
        c.setUnavailableCRLPolicy(getRevocationPolicy(x509.getCrlResourceUnavailablePolicy()));

        final Set<Resource> x509CrlResources = Sets.newLinkedHashSet();
        x509.getCrlResources()
                .stream()
                .map(s -> this.resourceLoader.getResource(s))
                .forEach(r -> x509CrlResources.add(r));
        c.setResources(x509CrlResources);

        c.setFetcher(getCrlFetcher());

        return c;
    }

    private static RevocationPolicy getRevocationPolicy(final String policy) {
        switch (policy.toLowerCase()) {
            case "allow":
                return new AllowRevocationPolicy();
            case "threshold":
                return new ThresholdExpiredCRLRevocationPolicy();
            case "deny":
            default:
                return new DenyRevocationPolicy();
        }
    }

    
    private CRLFetcher getCrlFetcher() {
        final X509Properties x509 = casProperties.getAuthn().getX509();
        switch (x509.getCrlFetcher().toLowerCase()) {
            case "ldap":
                return ldaptiveResourceCRLFetcher();
            case "resource":
            default:
                return resourceCrlFetcher();
        }
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler x509CredentialsAuthenticationHandler() {
        final X509Properties x509 = casProperties.getAuthn().getX509();
        final X509CredentialsAuthenticationHandler h = new X509CredentialsAuthenticationHandler();

        h.setCheckKeyUsage(x509.isCheckKeyUsage());
        h.setMaxPathLength(x509.getMaxPathLength());
        h.setMaxPathLengthAllowUnspecified(x509.isMaxPathLengthAllowUnspecified());
        h.setRequireKeyUsage(x509.isRequireKeyUsage());

        switch (x509.getRevocationChecker().toLowerCase()) {
            case "resource":
                h.setRevocationChecker(resourceCrlRevocationChecker());
                break;
            case "crl":
                h.setRevocationChecker(crlDistributionPointRevocationChecker());
                break;
            case "none":
            default:
                h.setRevocationChecker(noOpRevocationChecker());
                break;
        }

        if (StringUtils.isNotBlank(x509.getRegExTrustedIssuerDnPattern())) {
            h.setTrustedIssuerDnPattern(x509.getRegExTrustedIssuerDnPattern());
        }
        if (StringUtils.isNotBlank(x509.getRegExSubjectDnPattern())) {
            h.setTrustedIssuerDnPattern(x509.getRegExSubjectDnPattern());
        }
        h.setPrincipalFactory(x509PrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }

    @Bean
    public CRLFetcher ldaptiveResourceCRLFetcher() {
        final X509Properties x509 = casProperties.getAuthn().getX509();
        final LdaptiveResourceCRLFetcher r = new LdaptiveResourceCRLFetcher();
        r.setConnectionConfig(Beans.newConnectionConfig(x509.getLdap()));
        r.setSearchExecutor(Beans.newSearchExecutor(x509.getLdap().getBaseDn(), x509.getLdap().getSearchFilter()));
        return r;
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

    @PostConstruct
    public void initializeAuthenticationHandler() {

        PrincipalResolver resolver = personDirectoryPrincipalResolver;
        if (casProperties.getAuthn().getX509().getPrincipalType() != null) {
            switch (casProperties.getAuthn().getX509().getPrincipalType()) {
                case SERIAL_NO:
                    resolver = x509SerialNumberPrincipalResolver();
                    break;
                case SERIAL_NO_DN:
                    resolver = x509SerialNumberAndIssuerDNPrincipalResolver();
                    break;
                case SUBJECT:
                    resolver = x509SubjectPrincipalResolver();
                    break;
                case SUBJECT_ALT_NAME:
                    resolver = x509SubjectAlternativeNameUPNPrincipalResolver();
                    break;
                default:
                    resolver = x509SubjectDNPrincipalResolver();
                    break;
            }
        }

        this.authenticationHandlersResolvers.put(x509CredentialsAuthenticationHandler(), resolver);
    }
}
