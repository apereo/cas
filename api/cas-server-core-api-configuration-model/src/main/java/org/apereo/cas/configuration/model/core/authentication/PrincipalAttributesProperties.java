package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.support.azuread.AzureActiveDirectoryAttributesProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class PrincipalAttributesProperties implements Serializable {

    private static final long serialVersionUID = -4515569588579072890L;

    /**
     * Indicates the global cache expiration period, once attributes
     * are fetched from the underlying attribute repository.
     * A zero or negative value indicates that no attribute caching
     * should take place where attributes must always be fetched
     * from the source.
     */
    private int expirationTime = 30;

    /**
     * Expiration caching time unit for attributes.
     */
    private String expirationTimeUnit = TimeUnit.MINUTES.name();

    /**
     * Indicates the global cache size used to store attributes
     * retrieved from the attribute repository.
     */
    private int maximumCacheSize = 10000;

    /**
     * Merging strategies can be used to resolve conflicts when the same attribute are found from multiple sources.
     * Accepted values are the following:
     * <ul>
     * <li>{@code REPLACE}: Overwrites existing attribute values, if any.</li>
     * <li>{@code ADD}: Retains existing attribute values if any, and ignores
     * values from subsequent sources in the resolution chain.</li>
     * <li>{@code MULTIVALUED}: Combines all values into a single attribute, essentially creating a multi-valued attribute. </li>
     * <li>{@code NONE}: Doesn't merge attributes, ignores attributes from non-authentication attribute repositories </li>
     * </ul>
     */
    private String merger = "REPLACE";

    /**
     * Indicates how the results of multiple attribute repositories should
     * be aggregated together. Accepted values are {@code MERGE}, or {@code CASCADE}.
     * <ul>
     * <li>{@code MERGE}: Default. Designed to query multiple repositories
     * in order and merge the results into a single result set.</li>
     * <li>{@code CASCADE}: Query multiple repositories in order and merge the results into
     * a single result set. As each repository is queried
     * the attributes from the first query in the result set are
     * used as the query for the next repository. </li>
     * </ul>
     */
    private String aggregation = "MERGE";

    /**
     * CAS provides the ability to release a bundle of principal attributes to all services by default.
     * This bundle is not defined on a per-service basis and is always combined with attributes
     * produced by the specific release policy of the service, such that for instance,
     * you can devise rules to always release {@code givenName} and {@code cn} to every application,
     * and additionally allow other specific principal attributes for only some applications
     * per their attribute release policy.
     */
    private Set<String> defaultAttributesToRelease = new HashSet<>(0);

    /**
     * Retrieve attributes from multiple JDBC repositories.
     */
    private List<JdbcPrincipalAttributesProperties> jdbc = new ArrayList<>(0);

    /**
     * Retrieve attributes from multiple Microsoft Graph instances.
     */
    private List<AzureActiveDirectoryAttributesProperties> azureActiveDirectory = new ArrayList<>(0);

    /**
     * Retrieve attributes from multiple REST endpoints.
     */
    private List<RestPrincipalAttributesProperties> rest = new ArrayList<>(0);

    /**
     * Retrieve attributes from multiple Groovy scripts.
     */
    private List<GroovyPrincipalAttributesProperties> groovy = new ArrayList<>(0);

    /**
     * Retrieve attributes from multiple LDAP servers.
     */
    private List<LdapPrincipalAttributesProperties> ldap = new ArrayList<>(0);

    /**
     * Retrieve attributes from multiple JSON file repositories.
     */
    private List<JsonPrincipalAttributesProperties> json = new ArrayList<>(0);

    /**
     * Retrieve attributes from redis repositories.
     */
    private List<RedisPrincipalAttributesProperties> redis = new ArrayList<>(0);

    /**
     * Retrieve attributes from Couchbase repositories.
     */
    private CouchbasePrincipalAttributesProperties couchbase = new CouchbasePrincipalAttributesProperties();

    /**
     * Retrieve attributes from multiple scripted repositories.
     */
    private List<ScriptedPrincipalAttributesProperties> script = new ArrayList<>(0);

    /**
     * Use stubbed attribute definitions as the underlying attribute repository source.
     * Static attributes that need to be mapped to a hardcoded value belong here.
     */
    private StubPrincipalAttributesProperties stub = new StubPrincipalAttributesProperties();

    /**
     * Use Grouper to fetch principal attributes.
     * You will also need to ensure {@code grouper.client.properties}
     * is available on the classpath (i.e. {@code src/main/resources})
     * and it contains the following:
     * <pre>
     * grouperClient.webService.url = http://192.168.99.100:32768/grouper-ws/servicesRest
     * grouperClient.webService.login = banderson
     * grouperClient.webService.password = password
     * </pre>
     */
    private GrouperPrincipalAttributesProperties grouper = new GrouperPrincipalAttributesProperties();
}
