package org.apereo.cas.configuration.model.support.oauth;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link OAuthGrantsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Slf4j
@Getter
@Setter
public class OAuthGrantsProperties implements Serializable {

    private static final long serialVersionUID = -2246860215082703251L;

    /**
     * Resource owner grant settings.
     */
    private ResourceOwner resourceOwner = new ResourceOwner();

    @RequiresModule(name = "cas-server-support-oauth")
    @Getter
    @Setter
    public static class ResourceOwner implements Serializable {

        private static final long serialVersionUID = 3171206304518294330L;

        /**
         * Whether using the resource-owner grant should
         * enforce authorization rules and per-service policies
         * based on a service parameter is provided as a header
         * outside the normal semantics of the grant and protocol.
         */
        private boolean requireServiceHeader;
    }
}
