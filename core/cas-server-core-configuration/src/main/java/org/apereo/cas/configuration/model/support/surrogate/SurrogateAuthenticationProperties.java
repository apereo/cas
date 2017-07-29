package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.AbstractConfigProperties;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link SurrogateAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
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

    public static class Simple {
        /**
         * Define the list of accounts that are allowed to impersonate.
         * This is done in a key-value structure where the key is the admin user
         * and the value is a comma-separated list of identifiers that can be
         * impersonated by the adminuser.
         */
        private Map<String, String> surrogates = new LinkedHashMap<>();

        public Map<String, String> getSurrogates() {
            return surrogates;
        }

        public void setSurrogates(final Map<String, String> surrogates) {
            this.surrogates = surrogates;
        }
    }

    public static class Json extends AbstractConfigProperties {
        private static final long serialVersionUID = 3599367681439517829L;
    }

    public static class Ldap extends AbstractLdapProperties {
        private static final long serialVersionUID = -3848837302921751926L;
        /**
         * LDAP base DN used to locate the surrogate/admin accounts.
         */
        private String baseDn;
        /**
         * Search filter used to locate the admin user in the LDAP tree
         * and determine accounts qualified for impersonation.
         */
        private String searchFilter;
        /**
         * LDAP search filter used to locate the surrogate account.
         */
        private String surrogateSearchFilter;
        /**
         *  Attribute that must be found on the LDAP entry linked to the admin user
         *  that tags the account as authorized for impersonation.
         */
        private String memberAttributeName;
        /**
         * A pattern that is matched against the attribute value of the admin user,
         * that allows for further authorization of the admin user and accounts qualified for impersonation.
         * The regular expession pattern is expected to contain at least a single group whose value on a
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

    public static class Jdbc extends AbstractJpaProperties {
        private static final long serialVersionUID = 8970195444880123796L;

        /**
         * Surrogate query to use to determine whether an admin user can impersonate another user.
         * The query must return an integer count of greater than zero.
         */
        private String surrogateSearchQuery = "SELECT COUNT(*) FROM surrogate WHERE username=?";
        /**
         * SQL query to use in order to retrieve the list of qualified accounts for impersonation for a given admin user.
         */
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
