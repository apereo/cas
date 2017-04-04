package org.apereo.cas.configuration.model.support.x509;

import org.apereo.cas.configuration.model.core.authentication.PersonDirPrincipalResolverProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link X509Properties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class X509Properties {

    private static final String DENY = "DENY";

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
        SUBJECT_ALT_NAME
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

    private String serialNumberPrefix = "SERIALNUMBER=";
    private String valueDelimiter = ", ";
    private int revocationPolicyThreshold = 172_800;
    private boolean checkAll;
    private int refreshIntervalSeconds = 3_600;
    private String principalDescriptor;
    private boolean throwOnFetchFailure;
    private PrincipalTypes principalType;
    private String revocationChecker = "NONE";
    private String crlFetcher = "RESOURCE";
    private List<String> crlResources = new ArrayList<>();
    private int cacheMaxElementsInMemory = 1_000;
    private boolean cacheDiskOverflow;
    private boolean cacheEternal;
    private long cacheTimeToLiveSeconds = TimeUnit.HOURS.toSeconds(4);
    private long cacheTimeToIdleSeconds = TimeUnit.MINUTES.toSeconds(30);

    private String crlResourceUnavailablePolicy = DENY;
    private String crlResourceExpiredPolicy = DENY;
    private String crlUnavailablePolicy = DENY;
    private String crlExpiredPolicy = DENY;

    private int principalSNRadix;
    private boolean principalHexSNZeroPadding;
    
    @NestedConfigurationProperty
    private PersonDirPrincipalResolverProperties principal = new PersonDirPrincipalResolverProperties();
    
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

    private String regExSubjectDnPattern = ".+";

    private String trustedIssuerDnPattern = ".+";

    private String name;

    private String certificateAttribute = "certificateRevocationList";

    public String getCertificateAttribute() {
        return certificateAttribute;
    }

    public void setCertificateAttribute(final String certificateAttribute) {
        this.certificateAttribute = certificateAttribute;
    }
    
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

    public PersonDirPrincipalResolverProperties getPrincipal() {
        return principal;
    }
    
    public void setPrincipal(final PersonDirPrincipalResolverProperties principal) {
        this.principal = principal;
    }
    
    public String getTrustedIssuerDnPattern() {
        return trustedIssuerDnPattern;
    }

    public void setTrustedIssuerDnPattern(final String trustedIssuerDnPattern) {
        this.trustedIssuerDnPattern = trustedIssuerDnPattern;
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
        private String baseDn;
        private String searchFilter;
        
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
