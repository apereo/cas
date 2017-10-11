package org.apereo.cas.adaptors.x509.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.x509.authentication.CRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.ResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.handler.support.X509CredentialsAuthenticationHandler;
import org.apereo.cas.adaptors.x509.authentication.ldap.LdaptiveResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CommonNameEDIPIPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SerialNumberPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SubjectAlternativeNameUPNPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SubjectDNPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SubjectPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SerialNumberAndIssuerDNPrincipalResolver;
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
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.x509.X509Properties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import net.sf.ehcache.Cache;

/**
 * This is {@link X509AuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("x509AuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class X509AuthenticationConfiguration {

    private static final int HEX = 16;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

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
        return new ThresholdExpiredCRLRevocationPolicy(casProperties.getAuthn().getX509().getRevocationPolicyThreshold());
    }

    @Bean
    public RevocationPolicy denyRevocationPolicy() {
        return new DenyRevocationPolicy();
    }

    @Bean
    public RevocationChecker crlDistributionPointRevocationChecker() {
        final X509Properties x509 = casProperties.getAuthn().getX509();
        final Cache cache = new Cache("CRL".concat(UUID.randomUUID().toString()),
                x509.getCacheMaxElementsInMemory(),
                x509.isCacheDiskOverflow(),
                x509.isCacheEternal(),
                x509.getCacheTimeToLiveSeconds(),
                x509.getCacheTimeToIdleSeconds());

        return new CRLDistributionPointRevocationChecker(
                x509.isCheckAll(),
                getRevocationPolicy(x509.getCrlUnavailablePolicy()),
                getRevocationPolicy(x509.getCrlExpiredPolicy()),
                cache,
                crlFetcher(),
                x509.isThrowOnFetchFailure());
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
        final Set<Resource> x509CrlResources = x509.getCrlResources()
                .stream()
                .map(s -> this.resourceLoader.getResource(s))
                .collect(Collectors.toSet());

        return new ResourceCRLRevocationChecker(
                x509.isCheckAll(),
                getRevocationPolicy(x509.getCrlResourceUnavailablePolicy()),
                getRevocationPolicy(x509.getCrlResourceExpiredPolicy()),
                x509.getRefreshIntervalSeconds(),
                crlFetcher(),
                x509CrlResources);
    }

    private RevocationPolicy getRevocationPolicy(final String policy) {
        switch (policy.trim().toLowerCase()) {
            case "allow":
                return new AllowRevocationPolicy();
            case "threshold":
                return thresholdExpiredCRLRevocationPolicy();
            case "deny":
            default:
                return new DenyRevocationPolicy();
        }
    }

    @Bean
    public CRLFetcher crlFetcher() {
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
        final RevocationChecker revChecker;
        switch (x509.getRevocationChecker().trim().toLowerCase()) {
            case "resource":
                revChecker = resourceCrlRevocationChecker();
                break;
            case "crl":
                revChecker = crlDistributionPointRevocationChecker();
                break;
            case "none":
            default:
                revChecker = noOpRevocationChecker();
                break;
        }

        return new X509CredentialsAuthenticationHandler(
                x509.getName(),
                servicesManager,
                x509PrincipalFactory(),
                StringUtils.isNotBlank(x509.getRegExTrustedIssuerDnPattern())
                        ? RegexUtils.createPattern(x509.getRegExTrustedIssuerDnPattern()) : null,
                x509.getMaxPathLength(),
                x509.isMaxPathLengthAllowUnspecified(),
                x509.isCheckKeyUsage(),
                x509.isRequireKeyUsage(),
                StringUtils.isNotBlank(x509.getRegExSubjectDnPattern())
                        ? RegexUtils.createPattern(x509.getRegExSubjectDnPattern()) : null,
                revChecker);
    }

    @Bean
    public CRLFetcher ldaptiveResourceCRLFetcher() {
        final X509Properties x509 = casProperties.getAuthn().getX509();
        return new LdaptiveResourceCRLFetcher(LdapUtils.newLdaptiveConnectionConfig(x509.getLdap()),
                LdapUtils.newLdaptiveSearchExecutor(x509.getLdap().getBaseDn(), x509.getLdap().getSearchFilter()),
                x509.getLdap().getCertificateAttribute());
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectPrincipalResolver() {
        final X509Properties x509 = casProperties.getAuthn().getX509();
        final X509SubjectPrincipalResolver r = new X509SubjectPrincipalResolver(attributeRepository, 
                x509PrincipalFactory(),
                x509.getPrincipal().isReturnNull(),
                x509.getPrincipal().getPrincipalAttribute(),
                x509.getPrincipalDescriptor());
        return r;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectDNPrincipalResolver() {
        final X509Properties x509 = casProperties.getAuthn().getX509();
        final X509SubjectDNPrincipalResolver r = new X509SubjectDNPrincipalResolver(attributeRepository,
                x509PrincipalFactory(),
                x509.getPrincipal().isReturnNull(),
                x509.getPrincipal().getPrincipalAttribute());
        return r;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectAlternativeNameUPNPrincipalResolver() {
        final X509Properties x509 = casProperties.getAuthn().getX509();
        final X509SubjectAlternativeNameUPNPrincipalResolver r = new X509SubjectAlternativeNameUPNPrincipalResolver(attributeRepository,
                x509PrincipalFactory(),
                x509.getPrincipal().isReturnNull(),
                x509.getPrincipal().getPrincipalAttribute());
        return r;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SerialNumberPrincipalResolver() {
        final X509Properties x509 = casProperties.getAuthn().getX509();
        final X509SerialNumberPrincipalResolver r;
        final int radix = x509.getPrincipalSNRadix();
        if (Character.MIN_RADIX <= radix && radix <= Character.MAX_RADIX) {
            if (radix == HEX) {
                r = new X509SerialNumberPrincipalResolver(attributeRepository,
                        x509PrincipalFactory(),
                        x509.getPrincipal().isReturnNull(),
                        x509.getPrincipal().getPrincipalAttribute(),
                        radix, x509.isPrincipalHexSNZeroPadding());
            } else {
                r = new X509SerialNumberPrincipalResolver(attributeRepository,
                        x509PrincipalFactory(),
                        x509.getPrincipal().isReturnNull(),
                        x509.getPrincipal().getPrincipalAttribute(),
                        radix, false);
            }
        } else {
            r = new X509SerialNumberPrincipalResolver(attributeRepository,
                    x509PrincipalFactory(),
                    x509.getPrincipal().isReturnNull(),
                    x509.getPrincipal().getPrincipalAttribute());
        }
        return r;
    }

    @ConditionalOnMissingBean(name = "x509PrincipalFactory")
    @Bean
    public PrincipalFactory x509PrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SerialNumberAndIssuerDNPrincipalResolver() {
        final X509Properties x509 = casProperties.getAuthn().getX509();
        return new X509SerialNumberAndIssuerDNPrincipalResolver(attributeRepository,
                x509PrincipalFactory(),
                x509.getPrincipal().isReturnNull(),
                x509.getPrincipal().getPrincipalAttribute(),
                x509.getSerialNumberPrefix(), x509.getValueDelimiter());
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509CommonNameEDIPIPrincipalResolver() {
        final X509Properties x509 = casProperties.getAuthn().getX509();
        return new X509CommonNameEDIPIPrincipalResolver(attributeRepository,
                x509PrincipalFactory(),
                x509.getPrincipal().isReturnNull(),
                x509.getPrincipal().getPrincipalAttribute());
    }

    @ConditionalOnMissingBean(name = "x509AuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer x509AuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            PrincipalResolver resolver = null;
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
                    case CN_EDIPI:
                        resolver = x509CommonNameEDIPIPrincipalResolver();
                        break;
                    default:
                        resolver = x509SubjectDNPrincipalResolver();
                        break;
                }
            }

            plan.registerAuthenticationHandlerWithPrincipalResolver(x509CredentialsAuthenticationHandler(), resolver);
        };
    }
}
