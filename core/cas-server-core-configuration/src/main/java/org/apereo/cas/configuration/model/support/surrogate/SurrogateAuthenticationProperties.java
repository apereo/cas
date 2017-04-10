package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.AbstractConfigProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link SurrogateAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SurrogateAuthenticationProperties {
    private String separator = "+";
    private Simple simple = new Simple();
    private Json json = new Json();
    private Ldap ldap = new Ldap();

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
        private Map<String, String> surrogates = new LinkedHashMap<>();

        public Map<String, String> getSurrogates() {
            return surrogates;
        }

        public void setSurrogates(final Map<String, String> surrogates) {
            this.surrogates = surrogates;
        }
    }

    public static class Json extends AbstractConfigProperties {
    }

    public static class Ldap extends AbstractLdapProperties {
        private String baseDn;
        private String searchFilter;
        private String surrogateSearchFilter;
        private String memberAttributeName;
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
}
