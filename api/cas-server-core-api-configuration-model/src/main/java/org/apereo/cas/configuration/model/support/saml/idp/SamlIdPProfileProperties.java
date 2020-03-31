package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link SamlIdPProfileProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)
public class SamlIdPProfileProperties implements Serializable {

    private static final long serialVersionUID = -3218075783676789852L;

    /**
     * Settings related to the saml2 sso profile.
     */
    private Saml2SsoProfile sso = new Saml2SsoProfile();

    /**
     * Settings related to the saml2 sso post simple-sign profile.
     */
    private Saml2SsoPostSimpleSignProfile ssoPostSimpleSign = new Saml2SsoPostSimpleSignProfile();

    /**
     * Settings related to the saml2 slo redirect profile.
     */
    private Saml2SloProfile slo = new Saml2SloProfile();

    @RequiresModule(name = "cas-server-support-saml-idp")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Saml2SloProfile implements Serializable {
        private static final long serialVersionUID = 1976431439191949383L;

        /**
         * Whether the initial request should be explicitly url-decoded
         * before it's consumed by the decoder.
         */
        private boolean urlDecodeRedirectRequest;
    }

    @RequiresModule(name = "cas-server-support-saml-idp")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Saml2SsoProfile implements Serializable {
        private static final long serialVersionUID = 6576431439191949383L;

        /**
         * Whether the initial request should be explicitly url-decoded
         * before it's consumed by the decoder.
         */
        private boolean urlDecodeRedirectRequest;
    }

    @RequiresModule(name = "cas-server-support-saml-idp")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Saml2SsoPostSimpleSignProfile implements Serializable {
        private static final long serialVersionUID = 2276431439191949383L;

        /**
         * Whether the initial request should be explicitly url-decoded
         * before it's consumed by the decoder.
         */
        private boolean urlDecodeRedirectRequest;
    }
}
