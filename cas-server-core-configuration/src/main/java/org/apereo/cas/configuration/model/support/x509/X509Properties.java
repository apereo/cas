package org.apereo.cas.configuration.model.support.x509;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link X509Properties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas.x509.authn", ignoreUnknownFields = false)
public class X509Properties {

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
    private int revocationPolicyThreshold = 172800;
    private boolean checkAll;
    private int refreshIntervalSeconds = 3600;
    private String principalDescriptor;
    private boolean throwOnFetchFailure;

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
     * The compiled pattern for trusted DN's supplied by the deployer.
     */
    private String regExSubjectDnPattern = ".*";

    private String trustedIssuerDnPattern;

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

    public String getSerialNumberPrefix() {

        return serialNumberPrefix;
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
}
