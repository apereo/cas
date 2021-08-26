package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.support.azuread.AzureActiveDirectoryAttributesProperties;
import org.apereo.cas.configuration.model.support.couchbase.authentication.CouchbasePrincipalAttributesProperties;
import org.apereo.cas.configuration.model.support.jdbc.JdbcPrincipalAttributesProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapPrincipalAttributesProperties;
import org.apereo.cas.configuration.model.support.okta.OktaPrincipalAttributesProperties;
import org.apereo.cas.configuration.model.support.redis.RedisPrincipalAttributesProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
@JsonFilter("PrincipalAttributesProperties")
public class PrincipalAttributesProperties implements Serializable {

    private static final long serialVersionUID = -4515569588579072890L;
    
    /**
     * Attribute resolution core/common settings.
     */
    @NestedConfigurationProperty
    private PrincipalAttributesCoreProperties core = new PrincipalAttributesCoreProperties();

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
    @NestedConfigurationProperty
    private CouchbasePrincipalAttributesProperties couchbase = new CouchbasePrincipalAttributesProperties();

    /**
     * Retrieve attributes from multiple scripted repositories.
     * @deprecated Since 6.2
     */
    @Deprecated(since = "6.2")
    private List<ScriptedPrincipalAttributesProperties> script = new ArrayList<>(0);

    /**
     * Use stubbed attribute definitions as the underlying attribute repository source.
     * Static attributes that need to be mapped to a hardcoded value belong here.
     */
    @NestedConfigurationProperty
    private StubPrincipalAttributesProperties stub = new StubPrincipalAttributesProperties();

    /**
     * Use Grouper to fetch principal attributes.
     * You will also need to ensure {@code grouper.client.properties}
     * is available on the classpath (i.e. {@code src/main/resources})
     * and it contains the following:
     * <p>
     * {@code grouperClient.webService.url = http://192.168.99.100:32768/grouper-ws/servicesRest}
     * {@code grouperClient.webService.login = banderson}
     * {@code grouperClient.webService.password = password}
     */
    @NestedConfigurationProperty
    private GrouperPrincipalAttributesProperties grouper = new GrouperPrincipalAttributesProperties();

    /**
     * Reference to the attribute definition store
     * that contains metadata about attributes and their encoding specifics.
     */
    @NestedConfigurationProperty
    private AttributeDefinitionStoreProperties attributeDefinitionStore = new AttributeDefinitionStoreProperties();

    /**
     * Fetch user attributes from Okta.
     */
    @NestedConfigurationProperty
    private OktaPrincipalAttributesProperties okta = new OktaPrincipalAttributesProperties();
}
