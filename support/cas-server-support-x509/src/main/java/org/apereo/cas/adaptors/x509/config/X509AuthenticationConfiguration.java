package org.apereo.cas.adaptors.x509.config;

import org.apereo.cas.adaptors.x509.authentication.CRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.ResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.handler.support.X509CredentialsAuthenticationHandler;
import org.apereo.cas.adaptors.x509.authentication.ldap.LdaptiveResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CommonNameEDIPIPrincipalResolver;
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
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.x509.X509Properties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.RegexUtils;

import lombok.val;
import net.sf.ehcache.Cache;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.util.UUID;
import java.util.stream.Collectors;

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
        val x509 = casProperties.getAuthn().getX509();
        val cache = new Cache("CRL".concat(UUID.randomUUID().toString()),
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
        val x509 = casProperties.getAuthn().getX509();
        val x509CrlResources = x509.getCrlResources()
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
        val x509 = casProperties.getAuthn().getX509();
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
        val x509 = casProperties.getAuthn().getX509();
        val revChecker = getRevocationCheckerFrom(x509);
        return new X509CredentialsAuthenticationHandler(
            x509.getName(),
            servicesManager,
            x509PrincipalFactory(),
            StringUtils.isNotBlank(x509.getRegExTrustedIssuerDnPattern())
                ? RegexUtils.createPattern(x509.getRegExTrustedIssuerDnPattern())
                : null,
            x509.getMaxPathLength(),
            x509.isMaxPathLengthAllowUnspecified(),
            x509.isCheckKeyUsage(),
            x509.isRequireKeyUsage(),
            StringUtils.isNotBlank(x509.getRegExSubjectDnPattern())
                ? RegexUtils.createPattern(x509.getRegExSubjectDnPattern())
                : null,
            revChecker);
    }

    private RevocationChecker getRevocationCheckerFrom(final X509Properties x509) {
        val checker = x509.getRevocationChecker().trim().toLowerCase();
        if ("resource".equals(checker)) {
            return resourceCrlRevocationChecker();
        }
        if ("crl".equals(checker)) {
            return crlDistributionPointRevocationChecker();
        }
        return noOpRevocationChecker();
    }

    @Bean
    public CRLFetcher ldaptiveResourceCRLFetcher() {
        val x509 = casProperties.getAuthn().getX509();
        return new LdaptiveResourceCRLFetcher(LdapUtils.newLdaptiveConnectionConfig(x509.getLdap()),
            LdapUtils.newLdaptiveSearchExecutor(x509.getLdap().getBaseDn(), x509.getLdap().getSearchFilter()),
            x509.getLdap().getCertificateAttribute());
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectPrincipalResolver() {
        val x509 = casProperties.getAuthn().getX509();
        val r = new X509SubjectPrincipalResolver(attributeRepository,
            x509PrincipalFactory(),
            x509.getPrincipal().isReturnNull(),
            x509.getPrincipal().getPrincipalAttribute(),
            x509.getPrincipalDescriptor());
        return r;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectDNPrincipalResolver() {
        val x509 = casProperties.getAuthn().getX509();
        val r = new X509SubjectDNPrincipalResolver(attributeRepository,
            x509PrincipalFactory(),
            x509.getPrincipal().isReturnNull(),
            x509.getPrincipal().getPrincipalAttribute());
        return r;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectAlternativeNameUPNPrincipalResolver() {
        val x509 = casProperties.getAuthn().getX509();
        val r = new X509SubjectAlternativeNameUPNPrincipalResolver(attributeRepository,
            x509PrincipalFactory(),
            x509.getPrincipal().isReturnNull(),
            x509.getPrincipal().getPrincipalAttribute());
        return r;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SerialNumberPrincipalResolver() {
        val x509 = casProperties.getAuthn().getX509();
        return getX509SerialNumberPrincipalResolver(x509);
    }

    private X509SerialNumberPrincipalResolver getX509SerialNumberPrincipalResolver(final X509Properties x509) {
        val radix = x509.getPrincipalSNRadix();
        if (Character.MIN_RADIX <= radix && radix <= Character.MAX_RADIX) {
            if (radix == HEX) {
                return new X509SerialNumberPrincipalResolver(attributeRepository,
                    x509PrincipalFactory(),
                    x509.getPrincipal().isReturnNull(),
                    x509.getPrincipal().getPrincipalAttribute(),
                    radix, x509.isPrincipalHexSNZeroPadding());
            }
            return new X509SerialNumberPrincipalResolver(attributeRepository,
                x509PrincipalFactory(),
                x509.getPrincipal().isReturnNull(),
                x509.getPrincipal().getPrincipalAttribute(),
                radix, false);
        }
        return new X509SerialNumberPrincipalResolver(attributeRepository,
            x509PrincipalFactory(),
            x509.getPrincipal().isReturnNull(),
            x509.getPrincipal().getPrincipalAttribute());
    }

    @ConditionalOnMissingBean(name = "x509PrincipalFactory")
    @Bean
    public PrincipalFactory x509PrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SerialNumberAndIssuerDNPrincipalResolver() {
        val x509 = casProperties.getAuthn().getX509();
        return new X509SerialNumberAndIssuerDNPrincipalResolver(attributeRepository,
            x509PrincipalFactory(),
            x509.getPrincipal().isReturnNull(),
            x509.getPrincipal().getPrincipalAttribute(),
            x509.getSerialNumberPrefix(), x509.getValueDelimiter());
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509CommonNameEDIPIPrincipalResolver() {
        val x509 = casProperties.getAuthn().getX509();
        return new X509CommonNameEDIPIPrincipalResolver(attributeRepository,
            x509PrincipalFactory(),
            x509.getPrincipal().isReturnNull(),
            x509.getPrincipal().getPrincipalAttribute());
    }

    @ConditionalOnMissingBean(name = "x509AuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer x509AuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            val resolver = getPrincipalResolver();
            plan.registerAuthenticationHandlerWithPrincipalResolver(x509CredentialsAuthenticationHandler(), resolver);
        };
    }

    private PrincipalResolver getPrincipalResolver() {
        val type = casProperties.getAuthn().getX509().getPrincipalType();
        if (type == null) {
            return null;
        }
        if (type == X509Properties.PrincipalTypes.SERIAL_NO) {
            return x509SerialNumberPrincipalResolver();
        }
        if (type == X509Properties.PrincipalTypes.SERIAL_NO_DN) {
            return x509SerialNumberAndIssuerDNPrincipalResolver();
        }
        if (type == X509Properties.PrincipalTypes.SUBJECT) {
            return x509SubjectPrincipalResolver();
        }
        if (type == X509Properties.PrincipalTypes.SUBJECT_ALT_NAME) {
            return x509SubjectAlternativeNameUPNPrincipalResolver();
        }
        if (type == X509Properties.PrincipalTypes.CN_EDIPI) {
            return x509CommonNameEDIPIPrincipalResolver();
        }
        return x509SubjectDNPrincipalResolver();
    }
}
