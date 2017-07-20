package org.apereo.cas.configuration.model.support.mfa;

import java.io.Serializable;

/**
 * This is {@link BaseMultifactorProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseMultifactorProvider implements Serializable {
    private static final long serialVersionUID = -2690281104343633871L;
    /**
     * At times, CAS needs to determine the correct provider when step-up authentication is required.
     * Consider for a moment that CAS already has established an SSO session with/without a provider and has
     * reached a level of authentication. Another incoming request attempts to exercise that SSO
     * session with a different and often competing authentication requirement that may differ from the
     * authentication level CAS has already established. Concretely, examples may be:
     * <ul>
     * <li>CAS has achieved an SSO session, but a separate request now requires step-up authentication with DuoSecurity.</li>
     * <li>CAS has achieved an SSO session with an authentication level satisfied by DuoSecurity,
     * but a separate request now requires step-up authentication with YubiKey. </li>
     * </ul>
     * In certain scenarios, CAS will attempt to rank authentication levels and compare them with each other.
     * If CAS already has achieved a level that is higher than what the incoming request requires,
     * no step-up authentication will be performed. If the opposite is true, CAS will route the authentication
     * flow to the required authentication level and upon success, will adjust the SSO session with the
     * new higher authentication level now satisfied.
     * <p>
     * Ranking of authentication methods is done per provider via specific properties for each.
     * Note that the higher the rank value is, the higher on the security scale it remains.
     * A provider that ranks higher with a larger weight value trumps and override others with a lower value.
     * </p>
     */
    private int rank;
    /**
     * The identifier for the multifactor provider.
     * In most cases, this need not be configured explicitly, unless
     * multiple instances of the same provider type are configured in CAS.
     */
    private String id;
    /**
     * Multifactor bypass options for this provider.
     * Each multifactor provider is equipped with options to allow for MFA bypass. Once the provider is chosen to honor
     * the authentication request, bypass rules are then consulted to calculate whether the provider
     * should ignore the request and skip MFA conditionally.
     */
    private Bypass bypass = new Bypass();
    /**
     * The name of the authentication handler used to verify credentials in MFA.
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Bypass getBypass() {
        return bypass;
    }

    public void setBypass(final Bypass bypass) {
        this.bypass = bypass;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(final int rank) {
        this.rank = rank;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    /**
     * The bypass options for multifactor authentication.
     */
    public static class Bypass implements Serializable {
        private static final long serialVersionUID = -9181362378365850397L;
        /**
         * Skip multifactor authentication based on designated principal attribute names.
         */
        private String principalAttributeName;
        /**
         * Optionally, skip multifactor authentication based on designated principal attribute values.
         */
        private String principalAttributeValue;
        /**
         * Skip multifactor authentication based on designated authentication attribute names.
         */
        private String authenticationAttributeName;
        /**
         * Optionally, skip multifactor authentication based on designated authentication attribute values.
         */
        private String authenticationAttributeValue;
        /**
         * Skip multifactor authentication depending on form of primary authentication execution.
         * Specifically, skip multifactor if the a particular authentication handler noted by its name
         * successfully is able to authenticate credentials in the primary factor.
         */
        private String authenticationHandlerName;
        /**
         * Skip multifactor authentication depending on method/form of primary authentication execution.
         * Specifically, skip multifactor if the authentication method attribute collected as part of
         * authentication metadata matches a certain value.
         */
        private String authenticationMethodName;
        /**
         * Skip multifactor authentication depending on form of primary credentials.
         * Value must equal the fully qualified class name of the credential type.
         */
        private String credentialClassType;

        public String getCredentialClassType() {
            return credentialClassType;
        }

        public void setCredentialClassType(final String credentialClassType) {
            this.credentialClassType = credentialClassType;
        }

        public String getAuthenticationAttributeName() {
            return authenticationAttributeName;
        }

        public void setAuthenticationAttributeName(final String authenticationAttributeName) {
            this.authenticationAttributeName = authenticationAttributeName;
        }

        public String getAuthenticationAttributeValue() {
            return authenticationAttributeValue;
        }

        public void setAuthenticationAttributeValue(final String authenticationAttributeValue) {
            this.authenticationAttributeValue = authenticationAttributeValue;
        }

        public String getPrincipalAttributeName() {
            return principalAttributeName;
        }

        public void setPrincipalAttributeName(final String principalAttributeName) {
            this.principalAttributeName = principalAttributeName;
        }

        public String getPrincipalAttributeValue() {
            return principalAttributeValue;
        }

        public void setPrincipalAttributeValue(final String principalAttributeValue) {
            this.principalAttributeValue = principalAttributeValue;
        }

        public String getAuthenticationHandlerName() {
            return authenticationHandlerName;
        }

        public void setAuthenticationHandlerName(final String authenticationHandlerName) {
            this.authenticationHandlerName = authenticationHandlerName;
        }

        public String getAuthenticationMethodName() {
            return authenticationMethodName;
        }

        public void setAuthenticationMethodName(final String authenticationMethodName) {
            this.authenticationMethodName = authenticationMethodName;
        }
    }
}

