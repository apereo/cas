package org.apereo.cas.adaptors.x509.config;

import org.apereo.cas.adaptors.x509.authentication.CRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.ResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.handler.support.X509CredentialsAuthenticationHandler;
import org.apereo.cas.adaptors.x509.authentication.ldap.LdaptiveResourceCRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.principal.DefaultX509AttributeExtractor;
import org.apereo.cas.adaptors.x509.authentication.principal.EDIPIX509AttributeExtractor;
import org.apereo.cas.adaptors.x509.authentication.principal.X509AttributeExtractor;
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
import org.apereo.cas.adaptors.x509.util.X509AuthenticationUtils;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "x509AuthenticationConfiguration", proxyBeanMethods = false)
public class X509AuthenticationConfiguration {

    private static final int HEX = 16;

    private static X509SerialNumberPrincipalResolver getX509SerialNumberPrincipalResolver(final CasConfigurationProperties casProperties,
                                                                                          final IPersonAttributeDao attributeRepository,
                                                                                          final X509AttributeExtractor x509AttributeExtractor,
                                                                                          final PrincipalFactory x509PrincipalFactory) {
        val x509 = casProperties.getAuthn().getX509();
        val serialNoProperties = x509.getSerialNo();
        val personDirectory = casProperties.getPersonDirectory();
        val radix = serialNoProperties.getPrincipalSNRadix();
        val principal = x509.getPrincipal();
        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(x509PrincipalFactory,
            attributeRepository,
            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()),
            X509SerialNumberPrincipalResolver.class, principal, personDirectory);
        resolver.setX509AttributeExtractor(x509AttributeExtractor);
        if (Character.MIN_RADIX <= radix && radix <= Character.MAX_RADIX) {
            if (radix == HEX) {
                resolver.setRadix(radix);
                resolver.setZeroPadding(serialNoProperties.isPrincipalHexSNZeroPadding());
                return resolver;
            }
            resolver.setRadix(radix);
            return resolver;
        }
        return resolver;
    }

    private static RevocationChecker getRevocationCheckerFrom(final X509Properties x509,
                                                              final RevocationChecker resourceCrlRevocationChecker,
                                                              final RevocationChecker crlDistributionPointRevocationChecker,
                                                              final RevocationChecker noOpRevocationChecker) {
        val checker = x509.getRevocationChecker().trim();
        if ("resource".equalsIgnoreCase(checker)) {
            return resourceCrlRevocationChecker;
        }
        if ("crl".equalsIgnoreCase(checker)) {
            return crlDistributionPointRevocationChecker;
        }
        return noOpRevocationChecker;
    }

    private static RevocationPolicy getRevocationPolicy(final String policy,
                                                        final RevocationPolicy allowRevocationPolicy,
                                                        final RevocationPolicy thresholdExpiredCRLRevocationPolicy,
                                                        final RevocationPolicy denyRevocationPolicy) {
        switch (policy.trim().toLowerCase()) {
            case "allow":
                return allowRevocationPolicy;
            case "threshold":
                return thresholdExpiredCRLRevocationPolicy;
            case "deny":
            default:
                return denyRevocationPolicy;
        }
    }

    private static PrincipalResolver getPrincipalResolver(final CasConfigurationProperties casProperties,
                                                          final PrincipalResolver x509SerialNumberPrincipalResolver,
                                                          final PrincipalResolver x509SerialNumberAndIssuerDNPrincipalResolver,
                                                          final PrincipalResolver x509SubjectPrincipalResolver,
                                                          final PrincipalResolver x509SubjectAlternativeNameUPNPrincipalResolver,
                                                          final PrincipalResolver x509SubjectAlternativeNameRFC822EmailPrincipalResolver,
                                                          final PrincipalResolver x509SubjectDNPrincipalResolver,
                                                          final PrincipalResolver x509CommonNameEDIPIPrincipalResolver) {
        val type = casProperties.getAuthn().getX509().getPrincipalType();
        if (type == X509Properties.PrincipalTypes.SERIAL_NO) {
            return x509SerialNumberPrincipalResolver;
        }
        if (type == X509Properties.PrincipalTypes.SERIAL_NO_DN) {
            return x509SerialNumberAndIssuerDNPrincipalResolver;
        }
        if (type == X509Properties.PrincipalTypes.SUBJECT) {
            return x509SubjectPrincipalResolver;
        }
        if (type == X509Properties.PrincipalTypes.SUBJECT_ALT_NAME) {
            return x509SubjectAlternativeNameUPNPrincipalResolver;
        }
        if (type == X509Properties.PrincipalTypes.RFC822_EMAIL) {
            return x509SubjectAlternativeNameRFC822EmailPrincipalResolver;
        }
        if (type == X509Properties.PrincipalTypes.CN_EDIPI) {
            return x509CommonNameEDIPIPrincipalResolver;
        }
        return x509SubjectDNPrincipalResolver;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "allowRevocationPolicy")
    public RevocationPolicy allowRevocationPolicy() {
        return new AllowRevocationPolicy();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "thresholdExpiredCRLRevocationPolicy")
    @Autowired
    public RevocationPolicy thresholdExpiredCRLRevocationPolicy(final CasConfigurationProperties casProperties) {
        return new ThresholdExpiredCRLRevocationPolicy(casProperties.getAuthn().getX509().getRevocationPolicyThreshold());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "denyRevocationPolicy")
    public RevocationPolicy denyRevocationPolicy() {
        return new DenyRevocationPolicy();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "crlDistributionPointRevocationChecker")
    @Autowired
    public RevocationChecker crlDistributionPointRevocationChecker(final CasConfigurationProperties casProperties,
                                                                   @Qualifier("crlFetcher")
                                                                   final CRLFetcher crlFetcher,
                                                                   @Qualifier("allowRevocationPolicy")
                                                                   final RevocationPolicy allowRevocationPolicy,
                                                                   @Qualifier("thresholdExpiredCRLRevocationPolicy")
                                                                   final RevocationPolicy thresholdExpiredCRLRevocationPolicy,
                                                                   @Qualifier("denyRevocationPolicy")
                                                                   final RevocationPolicy denyRevocationPolicy) {
        val x509 = casProperties.getAuthn().getX509();
        var builder = UserManagedCacheBuilder.newUserManagedCacheBuilder(URI.class, byte[].class);
        if (x509.isCacheDiskOverflow()) {
            val capacity = Capacity.parse(x509.getCacheDiskSize());
            builder = builder.withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
                .disk(capacity.getSize().longValue(), MemoryUnit.valueOf(capacity.getUnitOfMeasure().name()), false));
        }
        builder = builder.withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder().heap(x509.getCacheMaxElementsInMemory(), EntryUnit.ENTRIES));
        if (x509.isCacheEternal()) {
            builder = builder.withExpiry(ExpiryPolicyBuilder.noExpiration());
        } else {
            builder = builder.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(x509.getCacheTimeToLiveSeconds())));
        }
        var cache = builder.build(true);
        return new CRLDistributionPointRevocationChecker(x509.isCheckAll(),
            getRevocationPolicy(x509.getCrlUnavailablePolicy(), allowRevocationPolicy, thresholdExpiredCRLRevocationPolicy, denyRevocationPolicy),
            getRevocationPolicy(x509.getCrlExpiredPolicy(), allowRevocationPolicy, thresholdExpiredCRLRevocationPolicy, denyRevocationPolicy),
            cache, crlFetcher, x509.isThrowOnFetchFailure());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "noOpRevocationChecker")
    public RevocationChecker noOpRevocationChecker() {
        return new NoOpRevocationChecker();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "resourceCrlRevocationChecker")
    @Autowired
    public RevocationChecker resourceCrlRevocationChecker(final CasConfigurationProperties casProperties,
                                                          final ConfigurableApplicationContext applicationContext,
                                                          @Qualifier("allowRevocationPolicy")
                                                          final RevocationPolicy allowRevocationPolicy,
                                                          @Qualifier("thresholdExpiredCRLRevocationPolicy")
                                                          final RevocationPolicy thresholdExpiredCRLRevocationPolicy,
                                                          @Qualifier("denyRevocationPolicy")
                                                          final RevocationPolicy denyRevocationPolicy,
                                                          @Qualifier("crlFetcher")
                                                          final CRLFetcher crlFetcher) {
        val x509 = casProperties.getAuthn().getX509();
        val x509CrlResources = x509.getCrlResources().stream().map(applicationContext::getResource).collect(Collectors.toSet());
        return new ResourceCRLRevocationChecker(x509.isCheckAll(),
            getRevocationPolicy(x509.getCrlResourceUnavailablePolicy(), allowRevocationPolicy, thresholdExpiredCRLRevocationPolicy, denyRevocationPolicy),
            getRevocationPolicy(x509.getCrlResourceExpiredPolicy(), allowRevocationPolicy, thresholdExpiredCRLRevocationPolicy, denyRevocationPolicy),
            x509.getRefreshIntervalSeconds(), crlFetcher, x509CrlResources);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "crlFetcher")
    @Autowired
    public CRLFetcher crlFetcher(final CasConfigurationProperties casProperties) {
        val x509 = casProperties.getAuthn().getX509();
        switch (x509.getCrlFetcher().toLowerCase()) {
            case "ldap":
                return new LdaptiveResourceCRLFetcher(LdapUtils.newLdaptiveConnectionConfig(x509.getLdap()),
                    LdapUtils.newLdaptiveSearchOperation(x509.getLdap().getBaseDn(), x509.getLdap().getSearchFilter()),
                    x509.getLdap().getCertificateAttribute());
            case "resource":
            default:
                return new ResourceCRLFetcher();
        }
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "x509CredentialsAuthenticationHandler")
    @Autowired
    public AuthenticationHandler x509CredentialsAuthenticationHandler(final CasConfigurationProperties casProperties,
                                                                      @Qualifier("resourceCrlRevocationChecker")
                                                                      final RevocationChecker resourceCrlRevocationChecker,
                                                                      @Qualifier("x509PrincipalFactory")
                                                                      final PrincipalFactory x509PrincipalFactory,
                                                                      @Qualifier("crlDistributionPointRevocationChecker")
                                                                      final RevocationChecker crlDistributionPointRevocationChecker,
                                                                      @Qualifier("noOpRevocationChecker")
                                                                      final RevocationChecker noOpRevocationChecker,
                                                                      @Qualifier(ServicesManager.BEAN_NAME)
                                                                      final ServicesManager servicesManager) {
        val x509 = casProperties.getAuthn().getX509();
        val revChecker = getRevocationCheckerFrom(x509, resourceCrlRevocationChecker, crlDistributionPointRevocationChecker, noOpRevocationChecker);
        val subjectDnPattern = StringUtils.isNotBlank(x509.getRegExSubjectDnPattern()) ? RegexUtils.createPattern(x509.getRegExSubjectDnPattern()) : null;
        val trustedIssuerDnPattern = StringUtils.isNotBlank(x509.getRegExTrustedIssuerDnPattern()) ? RegexUtils.createPattern(x509.getRegExTrustedIssuerDnPattern()) : null;

        return new X509CredentialsAuthenticationHandler(x509.getName(), servicesManager, x509PrincipalFactory, trustedIssuerDnPattern, x509.getMaxPathLength(),
            x509.isMaxPathLengthAllowUnspecified(), x509.isCheckKeyUsage(), x509.isRequireKeyUsage(), subjectDnPattern, revChecker, x509.getOrder());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "x509SubjectPrincipalResolver")
    @Autowired
    public PrincipalResolver x509SubjectPrincipalResolver(final CasConfigurationProperties casProperties,
                                                          @Qualifier("x509PrincipalFactory")
                                                          final PrincipalFactory x509PrincipalFactory,
                                                          @Qualifier("x509AttributeExtractor")
                                                          final X509AttributeExtractor x509AttributeExtractor,
                                                          @Qualifier("attributeRepository")
                                                          final IPersonAttributeDao attributeRepository) {
        val personDirectory = casProperties.getPersonDirectory();
        val x509 = casProperties.getAuthn().getX509();
        val principal = x509.getPrincipal();
        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(x509PrincipalFactory, attributeRepository,
            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()), X509SubjectPrincipalResolver.class, principal,
            personDirectory);
        resolver.setPrincipalDescriptor(x509.getPrincipalDescriptor());
        resolver.setX509AttributeExtractor(x509AttributeExtractor);
        return resolver;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "x509SubjectDNPrincipalResolver")
    @Autowired
    public PrincipalResolver x509SubjectDNPrincipalResolver(final CasConfigurationProperties casProperties,
                                                            @Qualifier("x509PrincipalFactory")
                                                            final PrincipalFactory x509PrincipalFactory,
                                                            @Qualifier("x509AttributeExtractor")
                                                            final X509AttributeExtractor x509AttributeExtractor,
                                                            @Qualifier("attributeRepository")
                                                            final IPersonAttributeDao attributeRepository) {
        val x509 = casProperties.getAuthn().getX509();
        val subjectDn = x509.getSubjectDn();
        val personDirectory = casProperties.getPersonDirectory();
        val principal = x509.getPrincipal();
        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(x509PrincipalFactory, attributeRepository,
            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()), X509SubjectDNPrincipalResolver.class, principal,
            personDirectory);
        resolver.setSubjectDnFormat(X509AuthenticationUtils.getSubjectDnFormat(subjectDn.getFormat()));
        resolver.setX509AttributeExtractor(x509AttributeExtractor);
        return resolver;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "x509SubjectAlternativeNameUPNPrincipalResolver")
    @Autowired
    public PrincipalResolver x509SubjectAlternativeNameUPNPrincipalResolver(final CasConfigurationProperties casProperties,
                                                                            @Qualifier("x509PrincipalFactory")
                                                                            final PrincipalFactory x509PrincipalFactory,
                                                                            @Qualifier("x509AttributeExtractor")
                                                                            final X509AttributeExtractor x509AttributeExtractor,
                                                                            @Qualifier("attributeRepository")
                                                                            final IPersonAttributeDao attributeRepository) {
        val x509 = casProperties.getAuthn().getX509();
        val personDirectory = casProperties.getPersonDirectory();
        val subjectAltNameProperties = x509.getSubjectAltName();
        val principal = x509.getPrincipal();
        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(x509PrincipalFactory, attributeRepository,
            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()), X509SubjectAlternativeNameUPNPrincipalResolver.class,
            principal, personDirectory);
        resolver.setAlternatePrincipalAttribute(subjectAltNameProperties.getAlternatePrincipalAttribute());
        resolver.setX509AttributeExtractor(x509AttributeExtractor);
        return resolver;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "x509SubjectAlternativeNameRFC822EmailPrincipalResolver")
    @Autowired
    public PrincipalResolver x509SubjectAlternativeNameRFC822EmailPrincipalResolver(final CasConfigurationProperties casProperties,
                                                                                    @Qualifier("x509PrincipalFactory")
                                                                                    final PrincipalFactory x509PrincipalFactory,
                                                                                    @Qualifier("x509AttributeExtractor")
                                                                                    final X509AttributeExtractor x509AttributeExtractor,
                                                                                    @Qualifier("attributeRepository")
                                                                                    final IPersonAttributeDao attributeRepository) {
        val x509 = casProperties.getAuthn().getX509();
        val personDirectory = casProperties.getPersonDirectory();
        val rfc822EmailProperties = x509.getRfc822Email();
        val principal = x509.getPrincipal();
        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(x509PrincipalFactory, attributeRepository,
            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()),
            X509SubjectAlternativeNameRFC822EmailPrincipalResolver.class,
            principal, personDirectory);
        resolver.setAlternatePrincipalAttribute(rfc822EmailProperties.getAlternatePrincipalAttribute());
        resolver.setX509AttributeExtractor(x509AttributeExtractor);
        return resolver;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "x509SerialNumberPrincipalResolver")
    @Autowired
    public PrincipalResolver x509SerialNumberPrincipalResolver(final CasConfigurationProperties casProperties,
                                                               @Qualifier("x509PrincipalFactory")
                                                               final PrincipalFactory x509PrincipalFactory,
                                                               @Qualifier("x509AttributeExtractor")
                                                               final X509AttributeExtractor x509AttributeExtractor,
                                                               @Qualifier("attributeRepository")
                                                               final IPersonAttributeDao attributeRepository) {
        return getX509SerialNumberPrincipalResolver(casProperties, attributeRepository, x509AttributeExtractor, x509PrincipalFactory);
    }

    @ConditionalOnMissingBean(name = "x509PrincipalFactory")
    @Bean
    public PrincipalFactory x509PrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "x509SerialNumberAndIssuerDNPrincipalResolver")
    @Autowired
    public PrincipalResolver x509SerialNumberAndIssuerDNPrincipalResolver(final CasConfigurationProperties casProperties,
                                                                          @Qualifier("x509PrincipalFactory")
                                                                          final PrincipalFactory x509PrincipalFactory,
                                                                          @Qualifier("x509AttributeExtractor")
                                                                          final X509AttributeExtractor x509AttributeExtractor,
                                                                          @Qualifier("attributeRepository")
                                                                          final IPersonAttributeDao attributeRepository) {
        val x509 = casProperties.getAuthn().getX509();
        val serialNoDnProperties = x509.getSerialNoDn();
        val principal = x509.getPrincipal();
        val personDirectory = casProperties.getPersonDirectory();
        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(x509PrincipalFactory, attributeRepository,
            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()), X509SerialNumberAndIssuerDNPrincipalResolver.class,
            principal, personDirectory);
        resolver.setSerialNumberPrefix(serialNoDnProperties.getSerialNumberPrefix());
        resolver.setValueDelimiter(serialNoDnProperties.getValueDelimiter());
        resolver.setX509AttributeExtractor(x509AttributeExtractor);
        return resolver;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "x509CommonNameEDIPIPrincipalResolver")
    @Autowired
    public PrincipalResolver x509CommonNameEDIPIPrincipalResolver(final CasConfigurationProperties casProperties,
                                                                  @Qualifier("x509PrincipalFactory")
                                                                  final PrincipalFactory x509PrincipalFactory,
                                                                  @Qualifier("x509AttributeExtractor")
                                                                  final X509AttributeExtractor x509AttributeExtractor,
                                                                  @Qualifier("attributeRepository")
                                                                  final IPersonAttributeDao attributeRepository) {
        val x509 = casProperties.getAuthn().getX509();
        val cnEdipiProperties = x509.getCnEdipi();
        val principal = x509.getPrincipal();
        val personDirectory = casProperties.getPersonDirectory();
        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(x509PrincipalFactory, attributeRepository,
            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()),
            X509CommonNameEDIPIPrincipalResolver.class, principal,
            personDirectory);
        resolver.setAlternatePrincipalAttribute(cnEdipiProperties.getAlternatePrincipalAttribute());
        resolver.setX509AttributeExtractor(x509AttributeExtractor);
        return resolver;
    }

    @ConditionalOnMissingBean(name = "x509AuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer x509AuthenticationEventExecutionPlanConfigurer(
        @Qualifier("x509SerialNumberPrincipalResolver")
        final PrincipalResolver x509SerialNumberPrincipalResolver,
        @Qualifier("x509SerialNumberAndIssuerDNPrincipalResolver")
        final PrincipalResolver x509SerialNumberAndIssuerDNPrincipalResolver,
        @Qualifier("x509SubjectPrincipalResolver")
        final PrincipalResolver x509SubjectPrincipalResolver,
        @Qualifier("x509SubjectAlternativeNameUPNPrincipalResolver")
        final PrincipalResolver x509SubjectAlternativeNameUPNPrincipalResolver,
        @Qualifier("x509SubjectAlternativeNameRFC822EmailPrincipalResolver")
        final PrincipalResolver x509SubjectAlternativeNameRFC822EmailPrincipalResolver,
        @Qualifier("x509SubjectDNPrincipalResolver")
        final PrincipalResolver x509SubjectDNPrincipalResolver,
        @Qualifier("x509CommonNameEDIPIPrincipalResolver")
        final PrincipalResolver x509CommonNameEDIPIPrincipalResolver,
        final CasConfigurationProperties casProperties,
        @Qualifier("x509CredentialsAuthenticationHandler")
        final AuthenticationHandler x509CredentialsAuthenticationHandler) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(x509CredentialsAuthenticationHandler,
            getPrincipalResolver(casProperties, x509SerialNumberPrincipalResolver, x509SerialNumberAndIssuerDNPrincipalResolver,
                x509SubjectPrincipalResolver, x509SubjectAlternativeNameUPNPrincipalResolver,
                x509SubjectAlternativeNameRFC822EmailPrincipalResolver, x509SubjectDNPrincipalResolver,
                x509CommonNameEDIPIPrincipalResolver));
    }

    @ConditionalOnMissingBean(name = "x509AttributeExtractor")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public X509AttributeExtractor x509AttributeExtractor(final CasConfigurationProperties casProperties) {
        val x509 = casProperties.getAuthn().getX509();
        if (x509.getCnEdipi().isExtractEdipiAsAttribute()) {
            return new EDIPIX509AttributeExtractor();
        }
        return new DefaultX509AttributeExtractor();
    }
}
