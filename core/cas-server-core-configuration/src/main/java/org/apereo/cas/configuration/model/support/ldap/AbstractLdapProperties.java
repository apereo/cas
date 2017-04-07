package org.apereo.cas.configuration.model.support.ldap;

import org.apache.commons.lang3.StringUtils;
import org.ldaptive.SearchScope;
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SecurityStrength;

import java.util.Arrays;
import java.util.List;

/**
 * This is {@link AbstractLdapProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractLdapProperties {
    /**
     * The ldap type used to handle specific ops.
     */
    public enum LdapType {
        /**
         * Generic ldap type (OpenLDAP, 389ds, etc).
         */
        GENERIC,
        /**
         * Active directory.
         */
        AD,
        /**
         * FreeIPA directory.
         */
        FreeIPA,
        /**
         * EDirectory.
         */
        EDirectory
    }

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

    /**
     * Describe ldap connection strategies.
     */
    public enum LdapConnectionStrategy {
        /**
         * Default JNDI.
         */
        DEFAULT,
        /**
         * First ldap used until it fails.
         */
        ACTIVE_PASSIVE,
        /**
         * Navigate the ldap url list for new connections and circle back.
         */
        ROUND_ROBIN,
        /**
         * Randomly pick a url.
         */
        RANDOM,
        /**
         * ldap urls based on DNS SRV records.
         */
        DNS_SRV
    }

    private String trustCertificates;

    private String keystore;
    private String keystorePassword;
    private String keystoreType;

    private int minPoolSize = 3;
    private int maxPoolSize = 10;
    private String poolPassivator = "BIND";

    private boolean validateOnCheckout = true;
    private boolean validatePeriodically = true;
    
    private String validateTimeout = "PT5S";
    private String validatePeriod = "PT5M";

    private boolean failFast = true;

    private String idleTime = "PT10M";
    private String prunePeriod = "PT2H";
    private String blockWaitTime = "PT3S";

    private String connectionStrategy;

    private String ldapUrl = "ldap://localhost:389";
    private boolean useSsl = true;
    private boolean useStartTls;
    private String connectTimeout = "PT5S";
    private String responseTimeout = "PT5S";

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

    private Validator validator = new Validator();

    private String name;

    public String getValidateTimeout() {
        return validateTimeout;
    }

    public void setValidateTimeout(final String validateTimeout) {
        this.validateTimeout = validateTimeout;
    }

    public String getPoolPassivator() {
        return poolPassivator;
    }

    public void setPoolPassivator(final String poolPassivator) {
        this.poolPassivator = poolPassivator;
    }

    public String getConnectionStrategy() {
        return connectionStrategy;
    }

    public void setConnectionStrategy(final String connectionStrategy) {
        this.connectionStrategy = connectionStrategy;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(final Validator validator) {
        this.validator = validator;
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

    public String getPrunePeriod() {
        return prunePeriod;
    }

    public void setPrunePeriod(final String prunePeriod) {
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

    public String getValidatePeriod() {
        return validatePeriod;
    }

    public void setValidatePeriod(final String validatePeriod) {
        this.validatePeriod = validatePeriod;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(final boolean failFast) {
        this.failFast = failFast;
    }

    public String getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(final String idleTime) {
        this.idleTime = idleTime;
    }

    public String getBlockWaitTime() {
        return blockWaitTime;
    }

    public void setBlockWaitTime(final String blockWaitTime) {
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

    public String getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(final String connectTimeout) {
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

    public String getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(final String responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public static class Validator {
        private String type = "search";
        private String baseDn = StringUtils.EMPTY;
        private String searchFilter = "(objectClass=*)";
        private SearchScope scope = SearchScope.OBJECT;
        private String attributeName = "objectClass";
        private List<String> attributeValues = Arrays.asList("top");
        private String dn = StringUtils.EMPTY;

        public String getDn() {
            return dn;
        }

        public void setDn(final String dn) {
            this.dn = dn;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public void setAttributeName(final String attributeName) {
            this.attributeName = attributeName;
        }

        public List<String> getAttributeValues() {
            return attributeValues;
        }

        public void setAttributeValues(final List<String> attributeValues) {
            this.attributeValues = attributeValues;
        }

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
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

        public SearchScope getScope() {
            return scope;
        }

        public void setScope(final SearchScope scope) {
            this.scope = scope;
        }
    }
}
