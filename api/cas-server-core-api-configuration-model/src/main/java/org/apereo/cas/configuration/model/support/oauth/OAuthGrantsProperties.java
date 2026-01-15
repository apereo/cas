package org.apereo.cas.configuration.model.support.oauth;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OAuthGrantsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Getter
@Setter
@Accessors(chain = true)
public class OAuthGrantsProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -2246860215082703251L;

    /**
     * Resource owner grant settings.
     */
    private ResourceOwner resourceOwner = new ResourceOwner();

    @RequiresModule(name = "cas-server-support-oauth")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ResourceOwner implements Serializable {

        @Serial
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
