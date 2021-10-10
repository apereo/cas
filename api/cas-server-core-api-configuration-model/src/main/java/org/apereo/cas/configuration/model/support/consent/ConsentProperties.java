package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link ConsentProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-consent-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("ConsentProperties")
public class ConsentProperties implements Serializable {

    private static final long serialVersionUID = 5201308051524438384L;

    /**
     * Path to script that determines the activation rules for consent-enabled
     * transactions.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties activationStrategyGroovyScript = new SpringResourceProperties();

    /**
     * Consent core settings.
     */
    @NestedConfigurationProperty
    private ConsentCoreProperties core = new ConsentCoreProperties();

    /**
     * Keep consent decisions stored via REST.
     */
    @NestedConfigurationProperty
    private RestfulConsentProperties rest = new RestfulConsentProperties();

    /**
     * Keep consent decisions stored via LDAP user records.
     */
    @NestedConfigurationProperty
    private LdapConsentProperties ldap = new LdapConsentProperties();

    /**
     * Keep consent decisions stored via JDBC resources.
     */
    @NestedConfigurationProperty
    private JpaConsentProperties jpa = new JpaConsentProperties();

    /**
     * Keep consent decisions stored via a static JSON resource.
     */
    @NestedConfigurationProperty
    private JsonConsentProperties json = new JsonConsentProperties();

    /**
     * Keep consent decisions stored via Redis.
     */
    @NestedConfigurationProperty
    private RedisConsentProperties redis = new RedisConsentProperties();

    /**
     * Keep consent decisions stored via a Groovy resource.
     */
    @NestedConfigurationProperty
    private GroovyConsentProperties groovy = new GroovyConsentProperties();

    /**
     * Keep consent decisions stored via a MongoDb database resource.
     */
    @NestedConfigurationProperty
    private MongoDbConsentProperties mongo = new MongoDbConsentProperties();

    /**
     * Keep consent decisions stored via a CouchDb database resource.
     */
    @NestedConfigurationProperty
    private CouchDbConsentProperties couchDb = new CouchDbConsentProperties();
}
