package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("Pac4jBaseClientProperties")
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
     * Indicate the title or display name of the client
     * for decoration and client presentation purposes.
     * If left blank, the client original name would be used by default.
     */
    private String displayName;

    /**
     * Determine how the callback url should be resolved.
     * Default is {@link CallbackUrlTypes#QUERY_PARAMETER}.
     */
    private CallbackUrlTypes callbackUrlType = CallbackUrlTypes.QUERY_PARAMETER;

    /**
     * Callback URL to use to return the flow
     * back to the CAS server one the identity
     * provider is successfully done. This may be
     * used at the discretion of the client and its type
     * to build service parameters, redirect URIs, etc.
     * If none is specified, the CAS server's login endpoint
     * will be used as the basis of the final callback url.
     */
    private String callbackUrl;

    /**
     * The callback url types.
     */
    public enum CallbackUrlTypes {
        /**
         * Path parameter callback url.
         * The client name is added to the path of the callback URL.
         */
        PATH_PARAMETER,
        /**
         * Query parameter callback url.
         * The client name is added to the path of a query parameter.
         */
        QUERY_PARAMETER,
        /**
         * No callback url.
         * No name is added to the callback URL to be able to distinguish the client.
         */
        NONE
    }
}
