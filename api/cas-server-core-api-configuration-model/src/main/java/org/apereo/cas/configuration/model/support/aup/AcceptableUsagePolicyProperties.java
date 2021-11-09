package org.apereo.cas.configuration.model.support.aup;

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
 * This is {@link AcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-aup-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AcceptableUsagePolicyProperties")
public class AcceptableUsagePolicyProperties implements Serializable {

    private static final long serialVersionUID = -7703477581675908899L;

    /**
     * Control AUP via LDAP.
     */
    private List<LdapAcceptableUsagePolicyProperties> ldap = new ArrayList<>();

    /**
     * Control AUP via JDBC.
     */
    @NestedConfigurationProperty
    private JdbcAcceptableUsagePolicyProperties jdbc = new JdbcAcceptableUsagePolicyProperties();

    /**
     * Control AUP via REST.
     */
    @NestedConfigurationProperty
    private RestAcceptableUsagePolicyProperties rest = new RestAcceptableUsagePolicyProperties();

    /**
     * Control AUP via CouchDb.
     */
    @NestedConfigurationProperty
    private CouchDbAcceptableUsagePolicyProperties couchDb = new CouchDbAcceptableUsagePolicyProperties();

    /**
     * Control AUP via Couchbase.
     */
    @NestedConfigurationProperty
    private CouchbaseAcceptableUsagePolicyProperties couchbase = new CouchbaseAcceptableUsagePolicyProperties();

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
     * Core configuration settings that control common AUP behavior
     * are captured here.
     */
    @NestedConfigurationProperty
    private AcceptableUsagePolicyCoreProperties core = new AcceptableUsagePolicyCoreProperties();

}
