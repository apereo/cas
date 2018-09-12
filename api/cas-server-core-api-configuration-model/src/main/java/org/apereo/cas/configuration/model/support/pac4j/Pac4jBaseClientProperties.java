package org.apereo.cas.configuration.model.support.pac4j;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link Pac4jBaseClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")

@Getter
@Setter
public class Pac4jBaseClientProperties implements Serializable {

    private static final long serialVersionUID = -7885975876831784206L;
    
    /**
     * Name of the client mostly for UI purposes and uniqueness.
     * This name, with 'non-word' characters converted to '-' (e.g. "This Org (New)" becomes "This-Org--New-")
     * is added to the "class" attribute of the redirect link on the login page, to allow for
     * custom styling of individual IdPs (e.g. for an organization logo).
     */
    private String clientName;
    
    /**
     * Auto-redirect to this client.
     */
    private boolean autoRedirect;

    /**
     * Create a callback url with the clientId in the path instead of in the querystring.
     */
    private boolean usePathBasedCallbackUrl;

    /**
     * The attribute to use as the principal identifier built during and upon a successful authentication attempt.
     */
    private String principalAttributeId;
}
