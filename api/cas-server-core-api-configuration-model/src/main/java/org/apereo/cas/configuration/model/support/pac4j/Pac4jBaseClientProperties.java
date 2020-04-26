package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@Accessors(chain = true)
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
     * The attribute to use as the principal identifier built during and upon a successful authentication attempt.
     */
    private String principalAttributeId;

    /**
     * Whether the client/external identity provider should be considered
     * active and enabled for integration purposes.
     */
    private boolean enabled = true;

    /**
     * CSS class that should be assigned to this client.
     */
    private String cssClass;

    /**
     * Determine how the callback url should be resolved.
     * Accepted values are:
     *
     * <ul>
     *     <li>{@code PATH_PARAMETER}: The client name is added to the path of the callback URL.</li>
     *     <li>{@code QUERY_PARAMETER}: The client name is added to the path of a query parameter.</li>
     *     <li>{@code NONE}: No name is added to the callback URL to be able to distinguish the client.</li>
     * </ul>
     * Default is {@link CallbackUrlTypes#QUERY_PARAMETER}.
     */
    private CallbackUrlTypes callbackUrlType = CallbackUrlTypes.QUERY_PARAMETER;
    
    /**
     * The callback url types.
     */
    public enum CallbackUrlTypes {
        /**
         * Path parameter callback url.
         */
        PATH_PARAMETER,
        /**
         * Query parameter callback url.
         */
        QUERY_PARAMETER,
        /**
         * No callback url.
         */
        NONE
    }
}
