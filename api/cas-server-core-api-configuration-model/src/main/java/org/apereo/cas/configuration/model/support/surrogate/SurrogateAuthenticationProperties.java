package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
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
 * This is {@link SurrogateAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-surrogate-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class SurrogateAuthenticationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -2088813217398883623L;

    /**
     * Core settings that drive surrogate authentication.
     */
    @NestedConfigurationProperty
    private SurrogateCoreAuthenticationProperties core = new SurrogateCoreAuthenticationProperties();

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
     * Locate surrogate accounts via a Groovy resource.
     */
    @NestedConfigurationProperty
    private SurrogateGroovyAuthenticationProperties groovy = new SurrogateGroovyAuthenticationProperties();

    /**
     * Locate surrogate accounts via an LDAP servers.
     */
    private List<SurrogateLdapAuthenticationProperties> ldap = new ArrayList<>();

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
