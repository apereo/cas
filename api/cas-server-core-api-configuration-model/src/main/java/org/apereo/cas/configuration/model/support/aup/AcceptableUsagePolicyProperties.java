package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-aup-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class AcceptableUsagePolicyProperties implements Serializable {

    private static final long serialVersionUID = -7703477581675908899L;

    /**
     * Control AUP via LDAP.
     */
    private List<LdapAcceptableUsagePolicyProperties> ldap = new ArrayList<>();

    /**
     * Control AUP via Redis.
     */
    @NestedConfigurationProperty
    private JdbcAcceptableUsagePolicyProperties jdbc = new JdbcAcceptableUsagePolicyProperties();

    /**
     * Control AUP via Redis.
     */
    @NestedConfigurationProperty
    private RestAcceptableUsagePolicyProperties rest = new RestAcceptableUsagePolicyProperties();

    /**
     * Control AUP via CouchDb.
     */
    @NestedConfigurationProperty
    private CouchDbAcceptableUsagePolicyProperties couchDb = new CouchDbAcceptableUsagePolicyProperties();

    /**
     * Control AUP via a MongoDb database resource.
     */
    @NestedConfigurationProperty
    private MongoDbAcceptableUsagePolicyProperties mongo = new MongoDbAcceptableUsagePolicyProperties();

    /**
     * Control AUP Groovy.
     */
    @NestedConfigurationProperty
    private GroovyAcceptableUsagePolicyProperties groovy = new GroovyAcceptableUsagePolicyProperties();

    /**
     * Control AUP via Redis.
     */
    @NestedConfigurationProperty
    private RedisAcceptableUsagePolicyProperties redis = new RedisAcceptableUsagePolicyProperties();

    /**
     * Control AUP backed by runtime's memory.
     */
    @NestedConfigurationProperty
    private InMemoryAcceptableUsagePolicyProperties inMemory = new InMemoryAcceptableUsagePolicyProperties();

    /**
     * Allows AUP to be turned off on startup.
     */
    private boolean enabled = true;

    /**
     * AUP attribute to choose in order to determine whether policy
     * has been accepted or not. The attribute is expected to contain
     * a boolean value where {@code true} indicates policy has been
     * accepted and {@code false} indicates otherwise.
     * The attribute is fetched for the principal from configured sources
     * and compared for the right match to determine policy status.
     * If the attribute is not found, the policy status is considered as denied.
     */
    private String aupAttributeName = "aupAccepted";

    /**
     * AUP attribute to choose whose single value dictates
     * how CAS should fetch the policy terms from
     * the relevant message bundles.
     */
    private String aupPolicyTermsAttributeName;

}
