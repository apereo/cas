package org.apereo.cas.configuration.model.support.x509;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link X509Properties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-x509-webflow")
public class X509Properties implements Serializable {
    
    private static final long serialVersionUID = -9032744084671270366L;

    /**
     * The  Principal types.
     */
    public enum PrincipalTypes {
        /**
         * Create principal by subject.
         */
        SUBJECT,
        /**
         * Create principal by subject DN.
         */
        SUBJECT_DN,
        /**
         * Create principal by serial no.
         */
        SERIAL_NO,
        /**
         * Create principal by serial no and DN.
         */
        SERIAL_NO_DN,
        /**
         * Create principal by subject alternative name.
         */
        SUBJECT_ALT_NAME,
        /**
         * Create principal by common name and EDIPI.
         */
        CN_EDIPI,
    }

    /**
     * Default setting to limit the number of intermediate certificates.
     */
    private static final int DEFAULT_MAXPATHLENGTH = 1;

    /**
     * Default setting whether to allow unspecified number of intermediate certificates.
     */
    private static final boolean DEFAULT_MAXPATHLENGTH_ALLOW_UNSPECIFIED = false;

    /**
     * Default setting to check keyUsage extension.
     */
    private static final boolean DEFAULT_CHECK_KEYUSAGE = false;

    /**
     * Default setting to force require "KeyUsage" extension.
     */
    private static final boolean DEFAULT_REQUIRE_KEYUSAGE = false;

    /**
     * The serial number prefix used for principal resolution
     * when type is set to {@link PrincipalTypes#SERIAL_NO_DN}.
     */
    private String serialNumberPrefix = "SERIALNUMBER=";
    /**
     * Value delimiter used for principal resolution
     * when type is set to {@link PrincipalTypes#SERIAL_NO_DN}.
     */
    private String valueDelimiter = ", ";
    /**
     * Threshold value if expired CRL revocation policy is to be handled via threshold.
     */
    private int revocationPolicyThreshold = 172_800;
    /**
     * Whether revocation checking should check all resources, or stop at first one.
     */
    private boolean checkAll;
    /**
     * The refresh interval of the internal scheduler in cases where CRL revocation checking
     * is done via resources.
     */
    private int refreshIntervalSeconds = 3_600;
    /**
     * The principal descriptor used for principal resolution
     * when type is set to {@link PrincipalTypes#SUBJECT}.
     */
    private String principalDescriptor;
    /**
     * When CRL revocation checking is done via distribution points,
     * decide if fetch failures should throw errors.
     */
    private boolean throwOnFetchFailure;
    /**
     * Indicates the type of principal resolution for X509.
     * <ul>
     * <li>{@code SERIAL_NO}: Resolve the principal by the serial number with a configurable radix,
     * ranging from 2 to 36. If radix is 16, then the serial number could be filled with leading zeros to even the number of digits.</li>
     * <li>{@code SERIAL_NO_DN}: Resolve the principal by serial number and issuer dn.</li>
     * <li>{@code SUBJECT}: Resolve the principal by extracting one or more attribute values from the
     * certificate subject DN and combining them with intervening delimiters.</li>
     * <li>{@code SUBJECT_ALT_NAME}: Resolve the principal by the subject alternative name extension.</li>
     * <li>{@code SUBJECT_DN}: The default type; Resolve the principal by the certificate’s subject dn.</li>
     * </ul>
     */
    private PrincipalTypes principalType;
    /**
     * Revocation certificate checking can be carried out in one of the following ways:
     * <ul>
     * <li>{@code NONE}: No revocation is performed.</li>
     * <li>{@code CRL}: The CRL URI(s) mentioned in the certificate cRLDistributionPoints extension field.
     * Caches are available to prevent excessive IO against CRL endpoints; CRL data is fetched if does not exist in the cache or if it is expired.</li>
     * <li>{@code RESOURCE}: A CRL hosted at a fixed location. The CRL is fetched at periodic intervals and cached.</li>
     * </ul>
     */
    private String revocationChecker = "NONE";
    /**
     * To fetch CRLs, the following options are available:
     * <ul>
     * <li>{@code RESOURCE}: By default, all revocation checks use fixed resources to fetch the CRL resource from the specified location.</li>
     * <li>{@code LDAP}: A CRL resource may be fetched from a pre-configured attribute, in the event that the CRL resource location is an LDAP URI.</li>
     * </ul>
     */
    private String crlFetcher = "RESOURCE";
    /**
     * List of CRL resources to use for fetching.
     */
    private List<String> crlResources = new ArrayList<>();
    /**
     * When CRLs are cached, indicate maximum number of elements kept in memory.
     */
    private int cacheMaxElementsInMemory = 1_000;
    /**
     * When CRLs are cached, indicate whether cache should overflow to disk.
     */
    private boolean cacheDiskOverflow;
    /**
     * When CRLs are cached, indicate if cache items should be eternal.
     */
    private boolean cacheEternal;
    /**
     * When CRLs are cached, indicate the time-to-live of cache items.
     */
    private long cacheTimeToLiveSeconds = TimeUnit.HOURS.toSeconds(4);
    /**
     * When CRLs are cached, indicate the idle timeout of cache items.
     */
    private long cacheTimeToIdleSeconds = TimeUnit.MINUTES.toSeconds(30);
    /**
     * If the CRL resource is unavailable, activate the this policy.
     * Activated if {@link #revocationChecker} is {@code RESOURCE}.
     * Accepted values are:
     * <ul>
     * <li>{@code ALLOW}: Allow authentication to proceed.</li>
     * <li>{@code DENY}: Deny authentication and block.</li>
     * <li>{@code THRESHOLD}: Applicable to CRL expiration, throttle the request whereby expired
     * data is permitted up to a threshold period of time but not afterward.</li>
     * </ul>
     */
    private String crlResourceUnavailablePolicy = "DENY";
    /**
     * If the CRL resource has expired, activate the this policy.
     * Activated if {@link #revocationChecker} is {@code RESOURCE}.
     * Accepted values are:
     * <ul>
     * <li>{@code ALLOW}: Allow authentication to proceed.</li>
     * <li>{@code DENY}: Deny authentication and block.</li>
     * <li>{@code THRESHOLD}: Applicable to CRL expiration, throttle the request whereby expired
     * data is permitted up to a threshold period of time but not afterward.</li>
     * </ul>
     */
    private String crlResourceExpiredPolicy = "DENY";
    /**
     * If the CRL is unavailable, activate the this policy.
     * Activated if {@link #revocationChecker} is {@code CRL}.
     * Accepted values are:
     * <ul>
     * <li>{@code ALLOW}: Allow authentication to proceed.</li>
     * <li>{@code DENY}: Deny authentication and block.</li>
     * <li>{@code THRESHOLD}: Applicable to CRL expiration, throttle the request whereby expired
     * data is permitted up to a threshold period of time but not afterward.</li>
     * </ul>
     */
    private String crlUnavailablePolicy = "DENY";
    /**
     * If the CRL has expired, activate the this policy.
     * Activated if {@link #revocationChecker} is {@code CRL}.
     * Accepted values are:
     * <ul>
     * <li>{@code ALLOW}: Allow authentication to proceed.</li>
     * <li>{@code DENY}: Deny authentication and block.</li>
     * <li>{@code THRESHOLD}: Applicable to CRL expiration, throttle the request whereby expired
     * data is permitted up to a threshold period of time but not afterward.</li>
     * </ul>
     */
    private String crlExpiredPolicy = "DENY";

    /**
     * Radix used when {@link #principalType} is {@link PrincipalTypes#SERIAL_NO}.
     */
    private int principalSNRadix;
    /**
     * If radix hex padding should be used when {@link #principalType} is {@link PrincipalTypes#SERIAL_NO}.
     */
    private boolean principalHexSNZeroPadding;

    /**
     * Principal resolution properties.
     */
    @NestedConfigurationProperty
    private PersonDirectoryPrincipalResolverProperties principal = new PersonDirectoryPrincipalResolverProperties();

    /**
     * LDAP settings when fetching CRLs from LDAP.
     */
    private Ldap ldap = new Ldap();

    /**
     * The compiled pattern supplied by the deployer.
     */
    private String regExTrustedIssuerDnPattern;

    /**
     * Deployer supplied setting for maximum pathLength in a SUPPLIED
     * certificate.
     */
    private int maxPathLength = DEFAULT_MAXPATHLENGTH;

    /**
     * Deployer supplied setting to allow unlimited pathLength in a SUPPLIED
     * certificate.
     */
    private boolean maxPathLengthAllowUnspecified = DEFAULT_MAXPATHLENGTH_ALLOW_UNSPECIFIED;

    /**
     * Deployer supplied setting to check the KeyUsage extension.
     */
    private boolean checkKeyUsage = DEFAULT_CHECK_KEYUSAGE;

    /**
     * Deployer supplied setting to force require the correct KeyUsage
     * extension.
     */
    private boolean requireKeyUsage = DEFAULT_REQUIRE_KEYUSAGE;

    /**
     * The pattern that authorizes an acceptable certificate by its subject dn.
     */
    private String regExSubjectDnPattern = ".+";

    /**
     * The authentication handler name.
     */
    private String name;
    

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getCacheMaxElementsInMemory() {
        return cacheMaxElementsInMemory;
    }

    public void setCacheMaxElementsInMemory(final int cacheMaxElementsInMemory) {
        this.cacheMaxElementsInMemory = cacheMaxElementsInMemory;
    }

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public boolean isCacheDiskOverflow() {
        return cacheDiskOverflow;
    }

    public void setCacheDiskOverflow(final boolean cacheDiskOverflow) {
        this.cacheDiskOverflow = cacheDiskOverflow;
    }

    public boolean isCacheEternal() {
        return cacheEternal;
    }

    public void setCacheEternal(final boolean cacheEternal) {
        this.cacheEternal = cacheEternal;
    }

    public long getCacheTimeToLiveSeconds() {
        return cacheTimeToLiveSeconds;
    }

    public void setCacheTimeToLiveSeconds(final long cacheTimeToLiveSeconds) {
        this.cacheTimeToLiveSeconds = cacheTimeToLiveSeconds;
    }

    public long getCacheTimeToIdleSeconds() {
        return cacheTimeToIdleSeconds;
    }

    public void setCacheTimeToIdleSeconds(final long cacheTimeToIdleSeconds) {
        this.cacheTimeToIdleSeconds = cacheTimeToIdleSeconds;
    }

    public String getCrlFetcher() {
        return crlFetcher;
    }

    public void setCrlFetcher(final String crlFetcher) {
        this.crlFetcher = crlFetcher;
    }

    public PersonDirectoryPrincipalResolverProperties getPrincipal() {
        return principal;
    }

    public void setPrincipal(final PersonDirectoryPrincipalResolverProperties principal) {
        this.principal = principal;
    }

    public String getRegExTrustedIssuerDnPattern() {
        return regExTrustedIssuerDnPattern;
    }

    public void setRegExTrustedIssuerDnPattern(final String regExTrustedIssuerDnPattern) {
        this.regExTrustedIssuerDnPattern = regExTrustedIssuerDnPattern;
    }

    public List<String> getCrlResources() {
        return crlResources;
    }

    public void setCrlResources(final List<String> crlResources) {
        this.crlResources = crlResources;
    }

    public int getMaxPathLength() {
        return maxPathLength;
    }

    public void setMaxPathLength(final int maxPathLength) {
        this.maxPathLength = maxPathLength;
    }

    public boolean isMaxPathLengthAllowUnspecified() {
        return maxPathLengthAllowUnspecified;
    }

    public void setMaxPathLengthAllowUnspecified(final boolean maxPathLengthAllowUnspecified) {
        this.maxPathLengthAllowUnspecified = maxPathLengthAllowUnspecified;
    }

    public boolean isCheckKeyUsage() {
        return checkKeyUsage;
    }

    public void setCheckKeyUsage(final boolean checkKeyUsage) {
        this.checkKeyUsage = checkKeyUsage;
    }

    public boolean isRequireKeyUsage() {
        return requireKeyUsage;
    }

    public void setRequireKeyUsage(final boolean requireKeyUsage) {
        this.requireKeyUsage = requireKeyUsage;
    }

    public String getRegExSubjectDnPattern() {
        return regExSubjectDnPattern;
    }

    public void setRegExSubjectDnPattern(final String regExSubjectDnPattern) {
        this.regExSubjectDnPattern = regExSubjectDnPattern;
    }

    public boolean isThrowOnFetchFailure() {
        return throwOnFetchFailure;
    }

    public void setThrowOnFetchFailure(final boolean throwOnFetchFailure) {
        this.throwOnFetchFailure = throwOnFetchFailure;
    }

    public String getPrincipalDescriptor() {
        return principalDescriptor;
    }

    public void setPrincipalDescriptor(final String principalDescriptor) {
        this.principalDescriptor = principalDescriptor;
    }

    public int getRefreshIntervalSeconds() {
        return refreshIntervalSeconds;
    }

    public void setRefreshIntervalSeconds(final int refreshIntervalSeconds) {
        this.refreshIntervalSeconds = refreshIntervalSeconds;
    }

    public boolean isCheckAll() {
        return checkAll;
    }

    public void setCheckAll(final boolean checkAll) {
        this.checkAll = checkAll;
    }

    public String getValueDelimiter() {
        return valueDelimiter;
    }

    public void setValueDelimiter(final String valueDelimiter) {
        this.valueDelimiter = valueDelimiter;
    }

    public String getRevocationChecker() {
        return revocationChecker;
    }

    public void setRevocationChecker(final String revocationChecker) {
        this.revocationChecker = revocationChecker;
    }

    public String getSerialNumberPrefix() {
        return serialNumberPrefix;
    }

    public PrincipalTypes getPrincipalType() {
        return principalType;
    }

    public void setPrincipalType(final PrincipalTypes principalType) {
        this.principalType = principalType;
    }

    public void setSerialNumberPrefix(final String serialNumberPrefix) {
        this.serialNumberPrefix = serialNumberPrefix;
    }

    public int getRevocationPolicyThreshold() {
        return revocationPolicyThreshold;
    }

    public void setRevocationPolicyThreshold(final int revocationPolicyThreshold) {
        this.revocationPolicyThreshold = revocationPolicyThreshold;
    }

    public String getCrlResourceUnavailablePolicy() {
        return crlResourceUnavailablePolicy;
    }

    public void setCrlResourceUnavailablePolicy(final String crlResourceUnavailablePolicy) {
        this.crlResourceUnavailablePolicy = crlResourceUnavailablePolicy;
    }

    public String getCrlResourceExpiredPolicy() {
        return crlResourceExpiredPolicy;
    }

    public void setCrlResourceExpiredPolicy(final String crlResourceExpiredPolicy) {
        this.crlResourceExpiredPolicy = crlResourceExpiredPolicy;
    }

    public String getCrlUnavailablePolicy() {
        return crlUnavailablePolicy;
    }

    public void setCrlUnavailablePolicy(final String crlUnavailablePolicy) {
        this.crlUnavailablePolicy = crlUnavailablePolicy;
    }

    public String getCrlExpiredPolicy() {
        return crlExpiredPolicy;
    }

    public void setCrlExpiredPolicy(final String crlExpiredPolicy) {
        this.crlExpiredPolicy = crlExpiredPolicy;
    }

    public int getPrincipalSNRadix() {
        return principalSNRadix;
    }

    public void setPrincipalSNRadix(final int principalSNRadix) {
        this.principalSNRadix = principalSNRadix;
    }

    public boolean isPrincipalHexSNZeroPadding() {
        return principalHexSNZeroPadding;
    }

    public void setPrincipalHexSNZeroPadding(final boolean principalHexSNZeroPadding) {
        this.principalHexSNZeroPadding = principalHexSNZeroPadding;
    }

    public static class Ldap extends AbstractLdapProperties {
        private static final long serialVersionUID = -1655068554291000206L;
        /**
         * The LDAP base dn to start the search.
         */
        private String baseDn;
        /**
         * The search filter. Example: {@code cn={user}}.
         */
        private String searchFilter;

        /**
         * The LDAP attribute that holds the certificate revocation list.
         */
        private String certificateAttribute = "certificateRevocationList";

        public String getCertificateAttribute() {
            return certificateAttribute;
        }

        public void setCertificateAttribute(final String certificateAttribute) {
            this.certificateAttribute = certificateAttribute;
        }
        
        public String getBaseDn() {
            return baseDn;
        }

        public void setBaseDn(final String baseDn) {
            this.baseDn = baseDn;
        }

        public String getSearchFilter() {
            return searchFilter;
        }

        public void setSearchFilter(final String searchFilter) {
            this.searchFilter = searchFilter;
        }
    }
}
