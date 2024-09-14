package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
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

public class AcceptableUsagePolicyProperties implements CasFeatureModule, Serializable {

    @Serial
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
     * Control AUP backed by runtime memory.
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
