package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link SurrogateAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-surrogate-webflow")
public class SurrogateAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = -2088813217398883623L;
    /**
     * The separator character used to distinguish between the surrogate account and the admin account.
     */
    private String separator = "+";
    /**
     * Locate surrogate accounts via CAS configuration, hardcoded as properties.
     */
    private Simple simple = new Simple();
    /**
     * Locate surrogate accounts via a JSON resource.
     */
    private Json json = new Json();
    /**
     * Locate surrogate accounts via an LDAP server.
     */
    private Ldap ldap = new Ldap();
    /**
     * Locate surrogate accounts via a JDBC resource.
     */
    private Jdbc jdbc = new Jdbc();

    /**
     * Locate surrogate accounts via a REST resource.
     */
    private Rest rest = new Rest();
    
    /**
     * Settings related to tickets issued for surrogate session, their expiration policy, etc.
     */
    private Tgt tgt = new Tgt();

    /**
     * Email settings for notifications.
     */
    @NestedConfigurationProperty
    private EmailProperties mail = new EmailProperties();

    /**
     * SMS settings for notifications.
     */
    @NestedConfigurationProperty
    private SmsProperties sms = new SmsProperties();

    public EmailProperties getMail() {
        return mail;
    }

    public void setMail(final EmailProperties mail) {
        this.mail = mail;
    }

    public SmsProperties getSms() {
        return sms;
    }

    public void setSms(final SmsProperties sms) {
        this.sms = sms;
    }
    
    public Rest getRest() {
        return rest;
    }

    public void setRest(final Rest rest) {
        this.rest = rest;
    }

    public Tgt getTgt() {
        return tgt;
    }

    public void setTgt(final Tgt tgt) {
        this.tgt = tgt;
    }

    public Jdbc getJdbc() {
        return jdbc;
    }

    public void setJdbc(final Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    public Simple getSimple() {
        return simple;
    }

    public void setSimple(final Simple simple) {
        this.simple = simple;
    }

    public Json getJson() {
        return json;
    }

    public void setJson(final Json json) {
        this.json = json;
    }

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(final String separator) {
        this.separator = separator;
    }

    @RequiresModule(name = "cas-server-support-surrogate-webflow")
    public static class Simple implements Serializable {
        private static final long serialVersionUID = 16938920863432222L;
        /**
         * Define the list of accounts that are allowed to impersonate.
         * This is done in a key-value structure where the key is the admin user
         * and the value is a comma-separated list of identifiers that can be
         * impersonated by the admin-user.
         */
        private Map<String, String> surrogates = new LinkedHashMap<>();

        public Map<String, String> getSurrogates() {
            return surrogates;
        }

        public void setSurrogates(final Map<String, String> surrogates) {
            this.surrogates = surrogates;
        }
    }

    @RequiresModule(name = "cas-server-support-surrogate-webflow")
    public static class Json extends SpringResourceProperties {
        private static final long serialVersionUID = 3599367681439517829L;
    }

    @RequiresModule(name = "cas-server-support-surrogate-authentication-rest")
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = 8152273816132989085L;
    }

    @RequiresModule(name = "cas-server-support-surrogate-authentication-ldap")
    public static class Ldap extends AbstractLdapProperties {
        private static final long serialVersionUID = -3848837302921751926L;
        /**
         * LDAP base DN used to locate the surrogate/admin accounts.
         */
        @RequiredProperty
        private String baseDn;
        /**
         * Search filter used to locate the admin user in the LDAP tree
         * and determine accounts qualified for impersonation.
         */
        @RequiredProperty
        private String searchFilter;
        /**
         * LDAP search filter used to locate the surrogate account.
         */
        private String surrogateSearchFilter;
        /**
         *  Attribute that must be found on the LDAP entry linked to the admin user
         *  that tags the account as authorized for impersonation.
         */
        @RequiredProperty
        private String memberAttributeName;
        /**
         * A pattern that is matched against the attribute value of the admin user,
         * that allows for further authorization of the admin user and accounts qualified for impersonation.
         * The regular expression pattern is expected to contain at least a single group whose value on a
         * successful match indicates the qualified impersonated user by admin.
         */
        private String memberAttributeValueRegex;

        public String getSurrogateSearchFilter() {
            return surrogateSearchFilter;
        }

        public void setSurrogateSearchFilter(final String surrogateSearchFilter) {
            this.surrogateSearchFilter = surrogateSearchFilter;
        }

        public String getMemberAttributeName() {
            return memberAttributeName;
        }

        public void setMemberAttributeName(final String memberAttributeName) {
            this.memberAttributeName = memberAttributeName;
        }

        public String getMemberAttributeValueRegex() {
            return memberAttributeValueRegex;
        }

        public void setMemberAttributeValueRegex(final String memberAttributeValueRegex) {
            this.memberAttributeValueRegex = memberAttributeValueRegex;
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

    @RequiresModule(name = "cas-server-support-surrogate-authentication")
    public static class Tgt implements Serializable {
        private static final long serialVersionUID = 2077366413438267330L;

        /**
         * Timeout in seconds to kill the surrogate session and consider tickets expired.
         */
        private long timeToKillInSeconds = 1_800;

        public long getTimeToKillInSeconds() {
            return timeToKillInSeconds;
        }

        public void setTimeToKillInSeconds(final long timeToKillInSeconds) {
            this.timeToKillInSeconds = timeToKillInSeconds;
        }
    }

    @RequiresModule(name = "cas-server-support-surrogate-authentication-jdbc")
    public static class Jdbc extends AbstractJpaProperties {
        private static final long serialVersionUID = 8970195444880123796L;

        /**
         * Surrogate query to use to determine whether an admin user can impersonate another user.
         * The query must return an integer count of greater than zero.
         */
        @RequiredProperty
        private String surrogateSearchQuery = "SELECT COUNT(*) FROM surrogate WHERE username=?";
        /**
         * SQL query to use in order to retrieve the list of qualified accounts for impersonation for a given admin user.
         */
        @RequiredProperty
        private String surrogateAccountQuery = "SELECT surrogate_user AS surrogateAccount FROM surrogate WHERE username=?";

        public String getSurrogateSearchQuery() {
            return surrogateSearchQuery;
        }

        public void setSurrogateSearchQuery(final String surrogateSearchQuery) {
            this.surrogateSearchQuery = surrogateSearchQuery;
        }

        public String getSurrogateAccountQuery() {
            return surrogateAccountQuery;
        }

        public void setSurrogateAccountQuery(final String surrogateAccountQuery) {
            this.surrogateAccountQuery = surrogateAccountQuery;
        }
    }
}
