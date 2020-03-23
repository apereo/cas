package org.apereo.cas.configuration.model.support.uma;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link UmaProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-oauth-uma")
@Getter
@Accessors(chain = true)
@Setter
public class UmaProperties implements Serializable {
    private static final long serialVersionUID = 865028615694269276L;

    /**
     * UMA issuer.
     */
    @RequiredProperty
    private String issuer = "http://localhost:8080/cas";

    /**
     * Handles settings related to permission tickets.
     */
    private PermissionTicket permissionTicket = new PermissionTicket();

    /**
     * Handles settings related to rpt tokens.
     */
    private RequestingPartyToken requestingPartyToken = new RequestingPartyToken();

    /**
     * Handles settings related to management of resource-sets, etc.
     */
    private ResourceSet resourceSet = new ResourceSet();

    @RequiresModule(name = "cas-server-support-oauth-uma")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class PermissionTicket implements Serializable {
        private static final long serialVersionUID = 6624128522839644377L;

        /**
         * Hard timeout to kill the access token and expire it.
         */
        private String maxTimeToLiveInSeconds = "PT3M";

    }

    @RequiresModule(name = "cas-server-support-oauth-uma")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ResourceSet implements Serializable {
        private static final long serialVersionUID = 215435145313504895L;

        /**
         * Store resource-sets and policies via JPA.
         */
        private Jpa jpa = new Jpa();

        @RequiresModule(name = "cas-server-support-oauth-uma")
        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Jpa extends AbstractJpaProperties {
            private static final long serialVersionUID = 210435146313504995L;

            public Jpa() {
                super.setUrl(StringUtils.EMPTY);
            }
        }
    }

    @RequiresModule(name = "cas-server-support-oauth-uma")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class RequestingPartyToken implements Serializable {
        private static final long serialVersionUID = 3988708361481340920L;

        /**
         * Hard timeout to kill the access token and expire it.
         */
        private String maxTimeToLiveInSeconds = "PT3M";

        /**
         * Path to the JWKS file that is used to sign the rpt token.
         */
        private transient Resource jwksFile = new FileSystemResource("/etc/cas/uma-keystore.jwks");
    }
}
