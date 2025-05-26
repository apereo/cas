package org.apereo.cas.configuration.model.core.slo;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link SingleLogoutProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class SingleLogoutProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 3676710533477055700L;

    /**
     * Whether SLO callbacks should be done in an asynchronous manner via the HTTP client.
     * When true, CAS will not wait for the operation to fully complete and will resume control to carry on.
     */
    private boolean asynchronous = true;

    /**
     * Whether SLO should be entirely disabled globally for the CAS deployment.
     */
    private boolean disabled;

    /**
     * Logout propagation type determines how SLO requests will be sent to applications.
     * This is especially applicable when SLO requests are processed using a front-channel mechanism.
     */
    private LogoutPropagationTypes logoutPropagationType = LogoutPropagationTypes.AJAX;

    /**
     * The Logout propagation types.
     */
    public enum LogoutPropagationTypes {
        /**
         * This is the default propagation mechanism where logout requests
         * are sent to applications using a AJAX call via {@code jsonp}.
         */
        AJAX,
        /**
         * The propagation mechanism will submit the logout request
         * to the logout URL that is loaded inside an iframe. This is typically
         * useful if the application receiving the logout request needs to generate HTML
         * to process the logout request notification, especially if the application is a SPA.
         */
        IFRAME
    }
}
