package org.apereo.cas.configuration.model.core.util;

import module java.base;
import org.apereo.cas.configuration.model.core.ticket.ProxyGrantingTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.ProxyTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.ServiceTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.TicketGrantingTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.TransientSessionTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.registry.TicketRegistryProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties class for {@code ticket}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class TicketProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 5586947805593202037L;

    /**
     * Properties and settings related to session-transient tickets.
     */
    @NestedConfigurationProperty
    private TransientSessionTicketProperties tst = new TransientSessionTicketProperties();

    /**
     * Properties and settings related to proxy-granting tickets.
     */
    @NestedConfigurationProperty
    private ProxyGrantingTicketProperties pgt = new ProxyGrantingTicketProperties();

    /**
     * Properties and settings related to ticket encryption.
     *
     * @deprecated since 7.1.0.
     */
    @Deprecated(since = "7.1.0")
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    /**
     * Properties and settings related to proxy tickets.
     */
    @NestedConfigurationProperty
    private ProxyTicketProperties pt = new ProxyTicketProperties();

    /**
     * Properties and settings related to ticket registry.
     */
    @NestedConfigurationProperty
    private TicketRegistryProperties registry = new TicketRegistryProperties();

    /**
     * Properties and settings related to service tickets.
     */
    @NestedConfigurationProperty
    private ServiceTicketProperties st = new ServiceTicketProperties();

    /**
     * Properties and settings related to ticket-granting tickets.
     */
    @NestedConfigurationProperty
    private TicketGrantingTicketProperties tgt = new TicketGrantingTicketProperties();

    /**
     * Indicates whether tickets issued and linked to a ticket-granting ticket
     * may also be tracked, and then removed as part of logout ops. There are a number of tickets
     * issued by CAS whose expiration policy is usually by default bound
     * to the SSO expiration policy and the active TGT, yet such tickets may be
     * allowed to live beyond the normal lifetime of a CAS SSO session
     * with options to be renewed. Examples include OAuth access tokens, etc.
     * Set this option to true if you want all linked tickets to be tracked and then removed.
     */
    private boolean trackDescendantTickets;

    public TicketProperties() {
        crypto.setEnabled(false);
        crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
