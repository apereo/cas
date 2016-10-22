package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.AbstractConfigProperties;
import org.apereo.services.persondir.support.QueryType;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link PrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PrincipalAttributesProperties {

    private int expireInMinutes = 30;
    private int maximumCacheSize = 10000;
    private String merger = "REPLACE";
    
    private Set<String> defaultAttributesToRelease = new HashSet<>();
    private Map<String, String> attributes = new HashMap();

    private Jdbc jdbc = new Jdbc();
    private Groovy groovy = new Groovy();
    private Ldap ldap = new Ldap();
    private Json json = new Json();

    public Groovy getGroovy() {
        return groovy;
    }

    public void setGroovy(final Groovy groovy) {
        this.groovy = groovy;
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

    public String getMerger() {
        return merger;
    }

    public void setMerger(final String merger) {
        this.merger = merger;
    }

    public int getExpireInMinutes() {
        return expireInMinutes;
    }

    public void setExpireInMinutes(final int expireInMinutes) {
        this.expireInMinutes = expireInMinutes;
    }

    public int getMaximumCacheSize() {
        return maximumCacheSize;
    }

    public void setMaximumCacheSize(final int maximumCacheSize) {
        this.maximumCacheSize = maximumCacheSize;
    }

    public Jdbc getJdbc() {
        return jdbc;
    }

    public void setJdbc(final Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(final Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Set<String> getDefaultAttributesToRelease() {
        return defaultAttributesToRelease;
    }

    public void setDefaultAttributesToRelease(final Set<String> defaultAttributesToRelease) {
        this.defaultAttributesToRelease = defaultAttributesToRelease;
    }

    public static class Jdbc extends AbstractJpaProperties {
        private String sql;
        private boolean singleRow = true;
        private boolean requireAllAttributes = true;
        private CaseCanonicalizationMode caseCanonicalization = CaseCanonicalizationMode.NONE;
        private QueryType queryType = QueryType.AND;
        private Map<String, String> columnMappings = new HashMap<>();
        private List<String> username = new ArrayList<>();

        public String getSql() {
            return sql;
        }

        public void setSql(final String sql) {
            this.sql = sql;
        }

        public List<String> getUsername() {
            return username;
        }

        public void setUsername(final List<String> username) {
            this.username = username;
        }

        public boolean isSingleRow() {
            return singleRow;
        }

        public void setSingleRow(final boolean singleRow) {
            this.singleRow = singleRow;
        }

        public boolean isRequireAllAttributes() {
            return requireAllAttributes;
        }

        public void setRequireAllAttributes(final boolean requireAllAttributes) {
            this.requireAllAttributes = requireAllAttributes;
        }

        public CaseCanonicalizationMode getCaseCanonicalization() {
            return caseCanonicalization;
        }

        public void setCaseCanonicalization(final CaseCanonicalizationMode caseCanonicalization) {
            this.caseCanonicalization = caseCanonicalization;
        }

        public QueryType getQueryType() {
            return queryType;
        }

        public void setQueryType(final QueryType queryType) {
            this.queryType = queryType;
        }

        public Map<String, String> getColumnMappings() {
            return columnMappings;
        }

        public void setColumnMappings(final Map<String, String> columnMappings) {
            this.columnMappings = columnMappings;
        }
    }

    public static class Json extends AbstractConfigProperties {
        
    }

    public static class Groovy extends AbstractConfigProperties {
        private boolean caseInsensitive;

        public boolean isCaseInsensitive() {
            return caseInsensitive;
        }

        public void setCaseInsensitive(final boolean caseInsensitive) {
            this.caseInsensitive = caseInsensitive;
        }
    }
    
    public static class Ldap extends AbstractLdapProperties {
        private boolean subtreeSearch = true;
        private String baseDn;
        private String userFilter;

        public String getBaseDn() {
            return baseDn;
        }

        public void setBaseDn(final String baseDn) {
            this.baseDn = baseDn;
        }

        public String getUserFilter() {
            return userFilter;
        }

        public void setUserFilter(final String userFilter) {
            this.userFilter = userFilter;
        }

        public boolean isSubtreeSearch() {
            return subtreeSearch;
        }

        public void setSubtreeSearch(final boolean subtreeSearch) {
            this.subtreeSearch = subtreeSearch;
        }
    }
}
