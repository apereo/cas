package org.apereo.cas.configuration.model.support.uma;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link UmaProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-oauth-uma")
@Getter
@Setter
public class UmaProperties implements Serializable {
    private static final long serialVersionUID = 865028615694269276L;

    /**
     * UMA issuer.
     */
    @RequiredProperty
    private String issuer = "http://localhost:8080/cas/uma";

    /**
     * Handles settings related to permission tickets.
     */
    private PermissionTicket permissionTicket = new PermissionTicket();

    @RequiresModule(name = "cas-server-support-oauth-uma")
    @Getter
    @Setter
    public static class PermissionTicket implements Serializable {
        private static final long serialVersionUID = 6624128522839644377L;

        /**
         * Hard timeout to kill the access token and expire it.
         */
        private String maxTimeToLiveInSeconds = "PT3M";

    }
}
