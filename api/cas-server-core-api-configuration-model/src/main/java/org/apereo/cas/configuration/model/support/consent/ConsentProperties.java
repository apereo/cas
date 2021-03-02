package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.web.flow.WebflowAutoConfigurationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;

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
     * Whether consent functionality should be enabled.
     */
    private boolean enabled = true;

    /**
     * Whether consent functionality should be globally
     * applicapable to all applications and requests.
     */
    private boolean active = true;

    /**
     * Global reminder time unit, to reconfirm consent
     * in cases no changes are detected.
     */
    private long reminder = 30;

    /**
     * Global reminder time unit of measure, to reconfirm consent
     * in cases no changes are detected.
     */
    private ChronoUnit reminderTimeUnit = ChronoUnit.DAYS;

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
     *  Keep consent decisions stored via Redis.
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

    /**
     * Signing/encryption settings.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    /**
     * The webflow configuration.
     */
    @NestedConfigurationProperty
    private WebflowAutoConfigurationProperties webflow = new WebflowAutoConfigurationProperties().setOrder(100);
    
    public ConsentProperties() {
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
