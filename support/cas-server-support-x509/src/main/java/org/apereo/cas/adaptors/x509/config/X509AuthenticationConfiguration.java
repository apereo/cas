package org.apereo.cas.adaptors.x509.config;

import org.apereo.cas.adaptors.x509.authentication.CRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.ResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.handler.support.X509CredentialsAuthenticationHandler;
import org.apereo.cas.adaptors.x509.authentication.ldap.LdaptiveResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CommonNameEDIPIPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SerialNumberAndIssuerDNPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SerialNumberPrincipalResolver;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SubjectAlternativeNameRFC822EmailPrincipalResolver;
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
import org.apereo.cas.configuration.model.support.x509.SubjectDnPrincipalResolverProperties;
import org.apereo.cas.configuration.model.support.x509.X509Properties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.model.Capacity;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import javax.security.auth.x500.X500Principal;
import java.net.URI;
import java.time.Duration;
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
    private ObjectProvider<IPersonAttributeDao> attributeRepository;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "allowRevocationPolicy")
    public RevocationPolicy allowRevocationPolicy() {
        return new AllowRevocationPolicy();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "thresholdExpiredCRLRevocationPolicy")
    public RevocationPolicy thresholdExpiredCRLRevocationPolicy() {
        return new ThresholdExpiredCRLRevocationPolicy(casProperties.getAuthn().getX509().getRevocationPolicyThreshold());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "denyRevocationPolicy")
    public RevocationPolicy denyRevocationPolicy() {
        return new DenyRevocationPolicy();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "crlDistributionPointRevocationChecker")
    public RevocationChecker crlDistributionPointRevocationChecker() {
        val x509 = casProperties.getAuthn().getX509();
        var builder = UserManagedCacheBuilder.newUserManagedCacheBuilder(URI.class, byte[].class);

        if (x509.isCacheDiskOverflow()) {
            val capacity = Capacity.parse(x509.getCacheDiskSize());
            builder = builder.withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
                .disk(capacity.getSize().longValue(), MemoryUnit.valueOf(capacity.getUnitOfMeasure().name()), false));
        }
        builder = builder.withResourcePools(
            ResourcePoolsBuilder.newResourcePoolsBuilder()
                .heap(x509.getCacheMaxElementsInMemory(), EntryUnit.ENTRIES));

        if (x509.isCacheEternal()) {
            builder = builder.withExpiry(ExpiryPolicyBuilder.noExpiration());
        } else {
            builder = builder.withExpiry(ExpiryPolicyBuilder
                .timeToLiveExpiration(Duration.ofSeconds(x509.getCacheTimeToLiveSeconds())));
        }
        var cache = builder.build(true);

        return new CRLDistributionPointRevocationChecker(
            x509.isCheckAll(),
            getRevocationPolicy(x509.getCrlUnavailablePolicy()),
            getRevocationPolicy(x509.getCrlExpiredPolicy()),
            cache,
            crlFetcher(),
            x509.isThrowOnFetchFailure());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "noOpRevocationChecker")
    public RevocationChecker noOpRevocationChecker() {
        return new NoOpRevocationChecker();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "resourceCrlRevocationChecker")
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

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "crlFetcher")
    public CRLFetcher crlFetcher() {
        val x509 = casProperties.getAuthn().getX509();
        switch (x509.getCrlFetcher().toLowerCase()) {
            case "ldap":
                return new LdaptiveResourceCRLFetcher(LdapUtils.newLdaptiveConnectionConfig(x509.getLdap()),
                    LdapUtils.newLdaptiveSearchOperation(x509.getLdap().getBaseDn(),
                        x509.getLdap().getSearchFilter()), x509.getLdap().getCertificateAttribute());
            case "resource":
            default:
                return new ResourceCRLFetcher();
        }
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "x509CredentialsAuthenticationHandler")
    public AuthenticationHandler x509CredentialsAuthenticationHandler() {
        val x509 = casProperties.getAuthn().getX509();
        val revChecker = getRevocationCheckerFrom(x509);
        val subjectDnPattern = StringUtils.isNotBlank(x509.getRegExSubjectDnPattern())
            ? RegexUtils.createPattern(x509.getRegExSubjectDnPattern())
            : null;
        val trustedIssuerDnPattern = StringUtils.isNotBlank(x509.getRegExTrustedIssuerDnPattern())
            ? RegexUtils.createPattern(x509.getRegExTrustedIssuerDnPattern())
            : null;

        return new X509CredentialsAuthenticationHandler(
            x509.getName(),
            servicesManager.getObject(),
            x509PrincipalFactory(),
            trustedIssuerDnPattern,
            x509.getMaxPathLength(),
            x509.isMaxPathLengthAllowUnspecified(),
            x509.isCheckKeyUsage(),
            x509.isRequireKeyUsage(),
            subjectDnPattern,
            revChecker,
            x509.getOrder());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "x509SubjectPrincipalResolver")
    public PrincipalResolver x509SubjectPrincipalResolver() {
        val personDirectory = casProperties.getPersonDirectory();
        val x509 = casProperties.getAuthn().getX509();
        val principal = x509.getPrincipal();
        val principalAttribute = StringUtils.defaultIfBlank(principal.getPrincipalAttribute(), personDirectory.getPrincipalAttribute());
        return new X509SubjectPrincipalResolver(
            attributeRepository.getObject(),
            x509PrincipalFactory(),
            principal.isReturnNull() || personDirectory.isReturnNull(),
            principalAttribute,
            x509.getPrincipalDescriptor(),
            principal.isUseExistingPrincipalId() || personDirectory.isUseExistingPrincipalId(),
            principal.isAttributeResolutionEnabled(),
            org.springframework.util.StringUtils.commaDelimitedListToSet(principal.getActiveAttributeRepositoryIds()));
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "x509SubjectDNPrincipalResolver")
    public PrincipalResolver x509SubjectDNPrincipalResolver() {
        val x509 = casProperties.getAuthn().getX509();
        val subjectDn = x509.getSubjectDn();
        val personDirectory = casProperties.getPersonDirectory();
        val principal = x509.getPrincipal();
        val principalAttribute = StringUtils.defaultIfBlank(principal.getPrincipalAttribute(), personDirectory.getPrincipalAttribute());
        return new X509SubjectDNPrincipalResolver(
            attributeRepository.getObject(),
            x509PrincipalFactory(),
            principal.isReturnNull() || personDirectory.isReturnNull(),
            principalAttribute,
            principal.isUseExistingPrincipalId() || personDirectory.isUseExistingPrincipalId(),
            principal.isAttributeResolutionEnabled(),
            org.springframework.util.StringUtils.commaDelimitedListToSet(principal.getActiveAttributeRepositoryIds()),
            getSubjectDnFormat(subjectDn.getFormat()));
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "x509SubjectAlternativeNameUPNPrincipalResolver")
    public PrincipalResolver x509SubjectAlternativeNameUPNPrincipalResolver() {
        val x509 = casProperties.getAuthn().getX509();
        val personDirectory = casProperties.getPersonDirectory();
        val subjectAltNameProperties = x509.getSubjectAltName();
        val principal = x509.getPrincipal();
        val principalAttribute = StringUtils.defaultIfBlank(principal.getPrincipalAttribute(), personDirectory.getPrincipalAttribute());
        return new X509SubjectAlternativeNameUPNPrincipalResolver(
            attributeRepository.getObject(),
            x509PrincipalFactory(),
            principal.isReturnNull() || personDirectory.isReturnNull(),
            principalAttribute,
            subjectAltNameProperties.getAlternatePrincipalAttribute(),
            principal.isUseExistingPrincipalId() || personDirectory.isUseExistingPrincipalId(),
            principal.isAttributeResolutionEnabled(),
            org.springframework.util.StringUtils.commaDelimitedListToSet(principal.getActiveAttributeRepositoryIds()));
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "x509SubjectAlternativeNameRFC822EmailPrincipalResolver")
    public PrincipalResolver x509SubjectAlternativeNameRFC822EmailPrincipalResolver() {
        val x509 = casProperties.getAuthn().getX509();
        val personDirectory = casProperties.getPersonDirectory();
        val rfc822EmailProperties = x509.getRfc822Email();
        val principal = x509.getPrincipal();
        val principalAttribute = StringUtils.defaultIfBlank(principal.getPrincipalAttribute(), personDirectory.getPrincipalAttribute());
        return new X509SubjectAlternativeNameRFC822EmailPrincipalResolver(
            attributeRepository.getObject(),
            x509PrincipalFactory(),
            principal.isReturnNull() || personDirectory.isReturnNull(),
            principalAttribute,
            rfc822EmailProperties.getAlternatePrincipalAttribute(),
            principal.isUseExistingPrincipalId() || personDirectory.isUseExistingPrincipalId(),
            principal.isAttributeResolutionEnabled(),
            org.springframework.util.StringUtils.commaDelimitedListToSet(principal.getActiveAttributeRepositoryIds()));
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "x509SerialNumberPrincipalResolver")
    public PrincipalResolver x509SerialNumberPrincipalResolver() {
        val x509 = casProperties.getAuthn().getX509();
        return getX509SerialNumberPrincipalResolver(x509);
    }

    @ConditionalOnMissingBean(name = "x509PrincipalFactory")
    @Bean
    public PrincipalFactory x509PrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "x509SerialNumberAndIssuerDNPrincipalResolver")
    public PrincipalResolver x509SerialNumberAndIssuerDNPrincipalResolver() {
        val x509 = casProperties.getAuthn().getX509();
        val serialNoDnProperties = x509.getSerialNoDn();
        val principal = x509.getPrincipal();
        val personDirectory = casProperties.getPersonDirectory();
        val principalAttribute = StringUtils.defaultIfBlank(principal.getPrincipalAttribute(), personDirectory.getPrincipalAttribute());

        return new X509SerialNumberAndIssuerDNPrincipalResolver(attributeRepository.getObject(),
            x509PrincipalFactory(),
            principal.isReturnNull() || personDirectory.isReturnNull(),
            principalAttribute,
            serialNoDnProperties.getSerialNumberPrefix(),
            serialNoDnProperties.getValueDelimiter(),
            principal.isUseExistingPrincipalId() || personDirectory.isUseExistingPrincipalId(),
            principal.isAttributeResolutionEnabled(),
            org.springframework.util.StringUtils.commaDelimitedListToSet(principal.getActiveAttributeRepositoryIds()));
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "x509CommonNameEDIPIPrincipalResolver")
    public PrincipalResolver x509CommonNameEDIPIPrincipalResolver() {
        val x509 = casProperties.getAuthn().getX509();
        val cnEdipiProperties = x509.getCnEdipi();
        val principal = x509.getPrincipal();
        val personDirectory = casProperties.getPersonDirectory();
        val principalAttribute = StringUtils.defaultIfBlank(principal.getPrincipalAttribute(), personDirectory.getPrincipalAttribute());
        return new X509CommonNameEDIPIPrincipalResolver(attributeRepository.getObject(),
            x509PrincipalFactory(),
            principal.isReturnNull() || personDirectory.isReturnNull(),
            principalAttribute,
            cnEdipiProperties.getAlternatePrincipalAttribute(),
            principal.isUseExistingPrincipalId() || personDirectory.isUseExistingPrincipalId(),
            principal.isAttributeResolutionEnabled(),
            org.springframework.util.StringUtils.commaDelimitedListToSet(principal.getActiveAttributeRepositoryIds()));
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
        if (type == X509Properties.PrincipalTypes.RFC822_EMAIL) {
            return x509SubjectAlternativeNameRFC822EmailPrincipalResolver();
        }
        if (type == X509Properties.PrincipalTypes.CN_EDIPI) {
            return x509CommonNameEDIPIPrincipalResolver();
        }
        return x509SubjectDNPrincipalResolver();
    }

    private static String getSubjectDnFormat(final SubjectDnPrincipalResolverProperties.SubjectDnFormat format) {
        switch (format) {
            case RFC1779:
                return X500Principal.RFC1779;
            case RFC2253:
                return X500Principal.RFC2253;
            case CANONICAL:
                return X500Principal.CANONICAL;
            default:
                return null;
        }
    }

    private X509SerialNumberPrincipalResolver getX509SerialNumberPrincipalResolver(final X509Properties x509) {
        val serialNoProperties = x509.getSerialNo();
        val personDirectory = casProperties.getPersonDirectory();
        val radix = serialNoProperties.getPrincipalSNRadix();
        val principal = x509.getPrincipal();
        val principalAttribute = StringUtils.defaultIfBlank(principal.getPrincipalAttribute(), personDirectory.getPrincipalAttribute());

        if (Character.MIN_RADIX <= radix && radix <= Character.MAX_RADIX) {
            if (radix == HEX) {
                return new X509SerialNumberPrincipalResolver(
                    attributeRepository.getObject(),
                    x509PrincipalFactory(),
                    principal.isReturnNull() || personDirectory.isReturnNull(),
                    principalAttribute,
                    radix, serialNoProperties.isPrincipalHexSNZeroPadding(),
                    principal.isUseExistingPrincipalId() || personDirectory.isUseExistingPrincipalId(),
                    principal.isAttributeResolutionEnabled(),
                    org.springframework.util.StringUtils.commaDelimitedListToSet(principal.getActiveAttributeRepositoryIds()));
            }
            return new X509SerialNumberPrincipalResolver(
                attributeRepository.getObject(),
                x509PrincipalFactory(),
                principal.isReturnNull() || personDirectory.isUseExistingPrincipalId(),
                principalAttribute,
                radix, false,
                principal.isUseExistingPrincipalId() || personDirectory.isUseExistingPrincipalId(),
                principal.isAttributeResolutionEnabled(),
                org.springframework.util.StringUtils.commaDelimitedListToSet(principal.getActiveAttributeRepositoryIds()));
        }
        return new X509SerialNumberPrincipalResolver(
            attributeRepository.getObject(),
            x509PrincipalFactory(),
            principal.isReturnNull() || personDirectory.isReturnNull(),
            principalAttribute,
            principal.isUseExistingPrincipalId() || personDirectory.isUseExistingPrincipalId(),
            principal.isAttributeResolutionEnabled(),
            org.springframework.util.StringUtils.commaDelimitedListToSet(principal.getActiveAttributeRepositoryIds()));
    }

    private RevocationChecker getRevocationCheckerFrom(final X509Properties x509) {
        val checker = x509.getRevocationChecker().trim();
        if ("resource".equalsIgnoreCase(checker)) {
            return resourceCrlRevocationChecker();
        }
        if ("crl".equalsIgnoreCase(checker)) {
            return crlDistributionPointRevocationChecker();
        }
        return noOpRevocationChecker();
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
}
