package org.apereo.cas.configuration.model.core.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.AbstractConfigProperties;
import org.apereo.services.persondir.support.QueryType;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link PrincipalAttributesProperties}.
 * Attribute sources are defined and configured to describe the global set of attributes to be fetched for each authenticated principal.
 * That global set of attributes is then filtered by the service manager according to service-specific attribute release rules.
 * The goal of the resolver is to construct a final identifiable authenticated principal for CAS
 * which carries a number of attributes inside it.
 * <p>The behavior of the resolver is such that it attempts to locate the principal id,
 * which in most cases is the same thing as the credential id provided
 * during authentication or it could be noted by a custom attribute.
 * Then the resolver starts to construct attributes from attribute repositories defined.
 * If it realizes that a custom attribute is used to determine the principal id
 * AND the same attribute is also set to be collected into the final set of attributes,
 * it will then remove that attribute from the final collection. </p>
 * Note that by default, CAS auto-creates attribute repository sources that are appropriate for LDAP, JDBC, etc.
 * If you need something more, you will need to resort to more elaborate measures of defining the bean configuration.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PrincipalAttributesProperties {

    /**
     * Indicates the global cache expiration period, once attributes
     * are fetched from the underlying attribute repository.
     */
    private int expireInMinutes = 30;

    /**
     * Indicates the global cache size used to store attributes
     * retrieved from the attribute repository.
     */
    private int maximumCacheSize = 10000;

    /**
     * Merging strategies can be used to resolve conflicts when the same attribute are found from multiple sources.
     * Accepted values are the following:
     * <ul>
     * <li><code>REPLACE</code>: Overwrites existing attribute values, if any.</li>
     * <li><code>ADD</code>: Retains existing attribute values if any, and ignores
     * values from subsequent sources in the resolution chain.</li>
     * <li><code>MERGE</code>: Combines all values into a single attribute, essentially creating a multi-valued attribute. </li>
     * </ul>
     */
    private String merger = "REPLACE";

    /**
     * CAS provides the ability to release a bundle of principal attributes to all services by default.
     * This bundle is not defined on a per-service basis and is always combined with attributes
     * produced by the specific release policy of the service, such that for instance,
     * you can devise rules to always release <code>givenName</code> and <code>cn</code> to every application,
     * and additionally allow other specific principal attributes for only some applications
     * per their attribute release policy.
     */
    private Set<String> defaultAttributesToRelease = new HashSet<>();

    /**
     * Retrieve attributes from multiple JDBC repositories.
     */
    private List<Jdbc> jdbc = new ArrayList<>();

    /**
     * Retrieve attributes from multiple REST endpoints.
     */
    private List<Rest> rest = new ArrayList<>();

    /**
     * Retrieve attributes from multiple Groovy scripts.
     */
    private List<Groovy> groovy = new ArrayList();

    /**
     * Retrieve attributes from multiple LDAP servers.
     */
    private List<Ldap> ldap = new ArrayList();

    /**
     * Retrieve attributes from multiple JSON file repositories.
     */
    private List<Json> json = new ArrayList();

    /**
     * Retrieve attributes from multiple scripted repositories.
     */
    private List<Script> script = new ArrayList<>();

    /**
     * Use stubbed attribute definitions as the underlying attribute repository source.
     * Static attributes that need to be mapped to a hardcoded value belong here.
     */
    private Stub stub = new Stub();

    /**
     * Use Grouper to fetch principal attributes.
     * You will also need to ensure <code>grouper.client.properties</code>
     * is available on the classpath (i.e. <code>src/main/resources</code>)
     * and it contains the following:
     * <pre>
     * grouperClient.webService.url = http://192.168.99.100:32768/grouper-ws/servicesRest
     * grouperClient.webService.login = banderson
     * grouperClient.webService.password = password
     * </pre>
     */
    private Grouper grouper = new Grouper();

    public List<Script> getScript() {
        return script;
    }

    public void setScript(final List<Script> script) {
        this.script = script;
    }

    public List<Rest> getRest() {
        return rest;
    }

    public void setRest(final List<Rest> rest) {
        this.rest = rest;
    }

    public Stub getStub() {
        return stub;
    }

    public void setStub(final Stub stub) {
        this.stub = stub;
    }

    public Grouper getGrouper() {
        return grouper;
    }

    public void setGrouper(final Grouper grouper) {
        this.grouper = grouper;
    }

    public List<Groovy> getGroovy() {
        return groovy;
    }

    public void setGroovy(final List<Groovy> groovy) {
        this.groovy = groovy;
    }

    public List<Json> getJson() {
        return json;
    }

    public void setJson(final List<Json> json) {
        this.json = json;
    }

    public List<Ldap> getLdap() {
        return ldap;
    }

    public void setLdap(final List<Ldap> ldap) {
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

    public List<Jdbc> getJdbc() {
        return jdbc;
    }

    public void setJdbc(final List<Jdbc> jdbc) {
        this.jdbc = jdbc;
    }

    public Set<String> getDefaultAttributesToRelease() {
        return defaultAttributesToRelease;
    }

    public void setDefaultAttributesToRelease(final Set<String> defaultAttributesToRelease) {
        this.defaultAttributesToRelease = defaultAttributesToRelease;
    }

    public static class Grouper {
        /**
         * The order of this attribute repository in the chain of repositories.
         * Can be used to explicitly position this source in chain and affects
         * merging strategies.
         */
        private int order;

        /**
         * Enable the attribute repository source.
         */
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(final int order) {
            this.order = order;
        }

    }

    public static class Stub {

        /**
         * Static attributes that need to be mapped to a hardcoded value belong here.
         * The structure follows a key-value pair where key is the attribute name
         * and value is the attribute value. The key is the attribute fetched
         * from the source and the value is the attribute name CAS should
         * use for virtual renames.
         */
        private Map<String, String> attributes = new HashMap();

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(final Map<String, String> attributes) {
            this.attributes = attributes;
        }
    }

    public static class Rest implements Serializable {
        private static final long serialVersionUID = -30055974448426360L;
        /**
         * The order of this attribute repository in the chain of repositories.
         * Can be used to explicitly position this source in chain and affects
         * merging strategies.
         */
        private int order;

        /**
         * The endpoint URL to contact and retrieve attributes.
         */
        private String url;

        /**
         * HTTP method to use when contacting the rest endpoint.
         * Examples include <code>GET, POST</code>, etc.
         */
        private String method;

        /**
         * Whether attribute repository should consider the underlying
         * attribute names in a case-insensitive manner.
         */
        private boolean caseInsensitive;

        /**
         * If REST endpoint is protected via basic authentication,
         * specify the username for authentication.
         */
        private String basicAuthUsername;
        /**
         * If REST endpoint is protected via basic authentication,
         * specify the password for authentication.
         */
        private String basicAuthPassword;

        public int getOrder() {
            return order;
        }

        public void setOrder(final int order) {
            this.order = order;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(final String url) {
            this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(final String method) {
            this.method = method;
        }

        public boolean isCaseInsensitive() {
            return caseInsensitive;
        }

        public void setCaseInsensitive(final boolean caseInsensitive) {
            this.caseInsensitive = caseInsensitive;
        }

        public String getBasicAuthUsername() {
            return basicAuthUsername;
        }

        public void setBasicAuthUsername(final String basicAuthUsername) {
            this.basicAuthUsername = basicAuthUsername;
        }

        public String getBasicAuthPassword() {
            return basicAuthPassword;
        }

        public void setBasicAuthPassword(final String basicAuthPassword) {
            this.basicAuthPassword = basicAuthPassword;
        }
    }

    public static class Jdbc extends AbstractJpaProperties {
        private static final long serialVersionUID = 6915428382578138387L;

        /**
         * The SQL statement to execute and fetch attributes.
         * The syntax of the query must be <code>SELECT * FROM table WHERE {0}</code>.
         * The <code>WHERE</code> clause is dynamically generated by CAS.
         */
        private String sql;
        /**
         * Designed to work against a table where there is a mapping of one row to one user.
         * The fields in the table structure is assumed to match <code>username|name|lastname|address</code>
         * where there is only a single row per user.
         * Setting this setting to <code>false</code> will force CAS to work against a table where
         * there is a mapping of one row to one user.
         * The fields in the table structure is assumed to match <code>username|attr_name|attr_value</code>
         * where there is more than one row per username.
         */
        private boolean singleRow = true;
        /**
         * If the SQL should only be run if all attributes listed in the mappings exist in the query.
         */
        private boolean requireAllAttributes = true;
        /**
         * When constructing the final person object from the attribute repository,
         * indicate how the username should be canonicalized.
         */
        private CaseCanonicalizationMode caseCanonicalization = CaseCanonicalizationMode.NONE;
        /**
         * Indicates how multiple attributes in a query should be concatenated together.
         * The other option is OR.
         */
        private QueryType queryType = QueryType.AND;

        /**
         * Used only when there is a mapping of many rows to one user.
         * This is done using a key-value structure where the key is the
         * name of the "attribute name" column  the value is the name of the "attribute value" column.
         * If the table structure is as such:
<pre>
-----------------------------
uid | attr_name  | attr_value
-----------------------------
tom | first_name | Thomas
</pre>
         * Then a column mapping must be specified to teach CAS to use <code>attr_name</code>
         * and <code>attr_value</code> for attribute names and values.
         */
        private Map<String, String> columnMappings = new HashMap<>();
        /**
         * Username attribute(s) to use when running the SQL query.
         */
        private List<String> username = new ArrayList<>();

        /**
         * The order of this attribute repository in the chain of repositories.
         * Can be used to explicitly position this source in chain and affects
         * merging strategies.
         */
        private int order;

        /**
         * Map of attributes to fetch from the database.
         * Attributes are defined using a key-value structure
         * where CAS allows the attribute name/key to be renamed virtually
         * to a different attribute. The key is the attribute fetched
         * from the data source and the value is the attribute name CAS should
         * use for virtual renames.
         */
        private Map<String, String> attributes = new HashMap();

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(final Map<String, String> attributes) {
            this.attributes = attributes;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(final int order) {
            this.order = order;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(final String sql) {
            this.sql = StringUtils.replace(sql, "{user}", "?");
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

    /**
     * If you wish to directly and separately retrieve attributes from a static JSON source.
     * The resource syntax must be as such:
<pre>
{
    "user1": {
        "firstName":["Json1"],
        "lastName":["One"]
    },
    "user2": {
        "firstName":["Json2"],
        "eduPersonAffiliation":["employee", "student"]
    }
}
</pre>
     */
    public static class Json extends AbstractConfigProperties {
        private static final long serialVersionUID = -6573755681498251678L;
        /**
         * The order of this attribute repository in the chain of repositories.
         * Can be used to explicitly position this source in chain and affects
         * merging strategies.
         */
        private int order;

        public int getOrder() {
            return order;
        }

        public void setOrder(final int order) {
            this.order = order;
        }
    }

    public static class Script extends AbstractConfigProperties {
        private static final long serialVersionUID = 4221139939506528713L;

        /**
         * Whether attribute repository should consider the underlying
         * attribute names in a case-insensitive manner.
         */
        private boolean caseInsensitive;
        /**
         * The order of this attribute repository in the chain of repositories.
         * Can be used to explicitly position this source in chain and affects
         * merging strategies.
         */
        private int order;

        public int getOrder() {
            return order;
        }

        public void setOrder(final int order) {
            this.order = order;
        }

        public boolean isCaseInsensitive() {
            return caseInsensitive;
        }

        public void setCaseInsensitive(final boolean caseInsensitive) {
            this.caseInsensitive = caseInsensitive;
        }
    }

    public static class Groovy extends AbstractConfigProperties {
        private static final long serialVersionUID = 7901595963842506684L;
        /**
         * Whether attribute repository should consider the underlying
         * attribute names in a case-insensitive manner.
         */
        private boolean caseInsensitive;
        /**
         * The order of this attribute repository in the chain of repositories.
         * Can be used to explicitly position this source in chain and affects
         * merging strategies.
         */
        private int order;

        public int getOrder() {
            return order;
        }

        public void setOrder(final int order) {
            this.order = order;
        }

        public boolean isCaseInsensitive() {
            return caseInsensitive;
        }

        public void setCaseInsensitive(final boolean caseInsensitive) {
            this.caseInsensitive = caseInsensitive;
        }
    }

    public static class Ldap extends AbstractLdapProperties {
        private static final long serialVersionUID = 5760065368731012063L;

        /**
         * Whether subtree searching should be perform recursively.
         */
        private boolean subtreeSearch = true;

        /**
         * Initial base DN to start the search.
         */
        private String baseDn;

        /**
         * Filter to query for user accounts.
         * Format must match <code>attributeName={user}</code>.
         */
        private String userFilter;

        /**
         * The order of this attribute repository in the chain of repositories.
         * Can be used to explicitly position this source in chain and affects
         * merging strategies.
         */
        private int order;

        /**
         * Map of attributes to fetch from the source.
         * Attributes are defined using a key-value structure
         * where CAS allows the attribute name/key to be renamed virtually
         * to a different attribute. The key is the attribute fetched
         * from the data source and the value is the attribute name CAS should
         * use for virtual renames.
         */
        private Map<String, String> attributes = new HashMap();

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(final Map<String, String> attributes) {
            this.attributes = attributes;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(final int order) {
            this.order = order;
        }

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
