package org.apereo.cas.configuration.model.core.logout;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link LogoutProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-logout", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class LogoutProperties implements Serializable {

    private static final long serialVersionUID = 7466171260665661949L;

    /**
     * The target destination to which CAS should redirect after logout
     * is indicated and extracted by a parameter name of your choosing here. If none specified,
     * the default will be used as {@code service}.
     */
    private String redirectParameter = "service";

    /**
     * Whether CAS should be allowed to redirect to an alternative location after logout.
     */
    private boolean followServiceRedirects;

    /**
     * Indicates whether tickets issued and linked to a ticket-granting ticket
     * should also be removed as part of logout. There are a number of tickets
     * issued by CAS whose expiration policy is usually by default bound
     * to the SSO expiration policy and the active TGT, yet such tickets may be
     * allowed to live beyond the normal lifetime of a CAS SSO session
     * with options to be renewed. Examples include OAuth's access tokens, etc.
     * Set this option to true if you want all linked tickets to be removed.
     */
    private boolean removeDescendantTickets;

    /**
     * Before logout, allow the option to confirm on the web interface.
     */
    private boolean confirmLogout;

    /**
     * A url to which CAS must immediately redirect
     * after all logout operations have completed.
     * Typically useful in scenarios where CAS is acting
     * as a proxy and needs to redirect to an external
     * identity provider's logout endpoint in order to
     * remove a session, etc.
     */
    private String redirectUrl;
}
