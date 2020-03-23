package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link OidcWebFingerProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcWebFingerProperties implements Serializable {

    private static final long serialVersionUID = 231228615694269276L;

    /**
     * Manage settings related to user-info repositories
     * locating resources and accounts.
     */
    private UserInfoRepository userInfo = new UserInfoRepository();

    @RequiresModule(name = "cas-server-support-oidc")
    @Setter
    @Accessors(chain = true)
    @Getter
    public static class UserInfoRepository implements Serializable {
        private static final long serialVersionUID = 1279027843747126043L;

        /**
         * Resolve webfinger user-info resources via REST.
         */
        private Rest rest = new Rest();

        /**
         * Resolve webfinger user-info resources via Groovy.
         */
        private Groovy groovy = new Groovy();
    }

    @RequiresModule(name = "cas-server-support-oidc")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Groovy extends SpringResourceProperties {
        private static final long serialVersionUID = 7179027843747126083L;
    }

    @RequiresModule(name = "cas-server-support-oidc")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = -2172345378378393382L;
    }
}
