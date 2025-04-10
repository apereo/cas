package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = 231228615694269276L;

    /**
     * Indicate if webfinger discovery protocol should be enabled.
     */
    private boolean enabled = true;

    /**
     * The regular expression pattern to use to match against the resource URL.
     */
    @RegularExpressionCapable
    private String resourcePattern = '^'
        + "((https|acct|http|mailto|tel|device):(//)?)?"
        + '('
        + "(([^@]+)@)?"
        + "(([^\\?#:/]+)"
        + "(:(\\d*))?)"
        + ')'
        + "([^\\?#]*)?"
        + "(\\?([^#]*))?"
        + "(#(.*))?"
        + '$';
    
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
        @Serial
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
        @Serial
        private static final long serialVersionUID = 7179027843747126083L;
    }

    @RequiresModule(name = "cas-server-support-oidc")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Rest extends RestEndpointProperties {
        @Serial
        private static final long serialVersionUID = -2172345378378393382L;
    }
}
