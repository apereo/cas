package org.apereo.cas.configuration.model.support.ldap;

import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SecurityStrength;

/**
 * This is {@link AbstractLdapProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractLdapProperties {
    /**
     * The ldap connection pool passivator.
     */
    public enum LdapConnectionPoolPassivator {
        /**
         * No passivator.
         */
        NONE,
        /**
         * Close passivator.
         */
        CLOSE,
        /**
         * Bind passivator.
         */
        BIND
    }
    
    private String trustCertificates;

    private String keystore;
    private String keystorePassword;
    private String keystoreType;

    private int minPoolSize = 3;
    private int maxPoolSize = 10;
    private String poolPassivator;
    
    private boolean validateOnCheckout = true;
    private boolean validatePeriodically = true;
    private long validatePeriod = 300;

    private boolean failFast = true;
    private long idleTime = 600;
    private long prunePeriod = 10000;
    private long blockWaitTime = 6000;
    
    private String ldapUrl = "ldap://localhost:389";
    private boolean useSsl = true;
    private boolean useStartTls;
    private long connectTimeout = 5000;

    private String providerClass;
    private boolean allowMultipleDns;

    private String bindDn;
    private String bindCredential;

    private String saslRealm;
    private Mechanism saslMechanism;
    private String saslAuthorizationId;

    private SecurityStrength saslSecurityStrength;
    private Boolean saslMutualAuth;
    private QualityOfProtection saslQualityOfProtection;

    public String getPoolPassivator() {
        return poolPassivator;
    }

    public void setPoolPassivator(final String poolPassivator) {
        this.poolPassivator = poolPassivator;
    }

    public String getBindDn() {
        return bindDn;
    }

    public void setBindDn(final String bindDn) {
        this.bindDn = bindDn;
    }

    public String getBindCredential() {
        return bindCredential;
    }

    public void setBindCredential(final String bindCredential) {
        this.bindCredential = bindCredential;
    }

    public String getProviderClass() {
        return providerClass;
    }

    public void setProviderClass(final String providerClass) {
        this.providerClass = providerClass;
    }

    public boolean isAllowMultipleDns() {
        return allowMultipleDns;
    }

    public void setAllowMultipleDns(final boolean allowMultipleDns) {
        this.allowMultipleDns = allowMultipleDns;
    }

    public long getPrunePeriod() {
        return prunePeriod;
    }

    public void setPrunePeriod(final long prunePeriod) {
        this.prunePeriod = prunePeriod;
    }

    public String getTrustCertificates() {
        return trustCertificates;
    }

    public void setTrustCertificates(final String trustCertificates) {
        this.trustCertificates = trustCertificates;
    }

    public String getKeystore() {
        return keystore;
    }

    public void setKeystore(final String keystore) {
        this.keystore = keystore;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(final String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(final String keystoreType) {
        this.keystoreType = keystoreType;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(final int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(final int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public boolean isValidateOnCheckout() {
        return validateOnCheckout;
    }

    public void setValidateOnCheckout(final boolean validateOnCheckout) {
        this.validateOnCheckout = validateOnCheckout;
    }

    public boolean isValidatePeriodically() {
        return validatePeriodically;
    }

    public void setValidatePeriodically(final boolean validatePeriodically) {
        this.validatePeriodically = validatePeriodically;
    }

    public long getValidatePeriod() {
        return validatePeriod;
    }

    public void setValidatePeriod(final long validatePeriod) {
        this.validatePeriod = validatePeriod;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(final boolean failFast) {
        this.failFast = failFast;
    }

    public long getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(final long idleTime) {
        this.idleTime = idleTime;
    }

    public long getBlockWaitTime() {
        return blockWaitTime;
    }

    public void setBlockWaitTime(final long blockWaitTime) {
        this.blockWaitTime = blockWaitTime;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }

    public void setLdapUrl(final String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public void setUseSsl(final boolean useSsl) {
        this.useSsl = useSsl;
    }

    public boolean isUseStartTls() {
        return useStartTls;
    }

    public void setUseStartTls(final boolean useStartTls) {
        this.useStartTls = useStartTls;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(final long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getSaslRealm() {
        return saslRealm;
    }

    public void setSaslRealm(final String saslRealm) {
        this.saslRealm = saslRealm;
    }

    public Mechanism getSaslMechanism() {
        return saslMechanism;
    }

    public void setSaslMechanism(final Mechanism saslMechanism) {
        this.saslMechanism = saslMechanism;
    }

    public String getSaslAuthorizationId() {
        return saslAuthorizationId;
    }

    public void setSaslAuthorizationId(final String saslAuthorizationId) {
        this.saslAuthorizationId = saslAuthorizationId;
    }

    public SecurityStrength getSaslSecurityStrength() {
        return saslSecurityStrength;
    }

    public void setSaslSecurityStrength(final SecurityStrength saslSecurityStrength) {
        this.saslSecurityStrength = saslSecurityStrength;
    }

    public QualityOfProtection getSaslQualityOfProtection() {
        return saslQualityOfProtection;
    }

    public void setSaslQualityOfProtection(final QualityOfProtection saslQualityOfProtection) {
        this.saslQualityOfProtection = saslQualityOfProtection;
    }

    public void setSaslMutualAuth(final Boolean saslMutualAuth) {
        this.saslMutualAuth = saslMutualAuth;
    }

    public Boolean getSaslMutualAuth() {
        return saslMutualAuth;
    }
}
