package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link SurrogateAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-surrogate-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("SurrogateAuthenticationProperties")
public class SurrogateAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = -2088813217398883623L;

    /**
     * The separator character used to distinguish between the surrogate account and the admin account.
     */
    private String separator = "+";

    /**
     * Locate surrogate accounts via CouchDB.
     */
    @NestedConfigurationProperty
    private SurrogateCouchDbAuthenticationProperties couchDb = new SurrogateCouchDbAuthenticationProperties();

    /**
     * Locate surrogate accounts via CAS configuration, hardcoded as properties.
     */
    @NestedConfigurationProperty
    private SurrogateSimpleAuthenticationProperties simple = new SurrogateSimpleAuthenticationProperties();

    /**
     * Locate surrogate accounts via a JSON resource.
     */
    @NestedConfigurationProperty
    private SurrogateJsonAuthenticationProperties json = new SurrogateJsonAuthenticationProperties();

    /**
     * Locate surrogate accounts via an LDAP server.
     */
    @NestedConfigurationProperty
    private SurrogateLdapAuthenticationProperties ldap = new SurrogateLdapAuthenticationProperties();

    /**
     * Locate surrogate accounts via a JDBC resource.
     */
    @NestedConfigurationProperty
    private SurrogateJdbcAuthenticationProperties jdbc = new SurrogateJdbcAuthenticationProperties();

    /**
     * Locate surrogate accounts via a REST resource.
     */
    @NestedConfigurationProperty
    private SurrogateRestfulAuthenticationProperties rest = new SurrogateRestfulAuthenticationProperties();

    /**
     * Settings related to tickets issued for surrogate session, their expiration policy, etc.
     */
    @NestedConfigurationProperty
    private SurrogateAuthenticationTicketGrantingTicketProperties tgt = new SurrogateAuthenticationTicketGrantingTicketProperties();

    /**
     * Principal construction settings.
     */
    @NestedConfigurationProperty
    private PersonDirectoryPrincipalResolverProperties principal = new PersonDirectoryPrincipalResolverProperties();

    /**
     * Email settings for notifications.
     */
    @NestedConfigurationProperty
    private EmailProperties mail = new EmailProperties();

    /**
     * SMS settings for notifications.
     */
    @NestedConfigurationProperty
    private SmsProperties sms = new SmsProperties();
}
