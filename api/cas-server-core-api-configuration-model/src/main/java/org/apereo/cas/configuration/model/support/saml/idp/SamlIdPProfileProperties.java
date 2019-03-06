package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

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
public class SamlIdPProfileProperties implements Serializable {

    private static final long serialVersionUID = -3218075783676789852L;

    /**
     * Settings related to the saml2 sso redirect profile.
     */
    private Saml2SsoProfileRedirect ssoRedirect = new Saml2SsoProfileRedirect();
    /**
     * Settings related to the saml2 sso post profile.
     */
    private Saml2SsoProfilePost ssoPost = new Saml2SsoProfilePost();
    /**
     * Settings related to the saml2 sso post simple-sign profile.
     */
    private Saml2SsoProfilePostSimpleSign ssoPostSimpleSign = new Saml2SsoProfilePostSimpleSign();

    /**
     * Settings related to the saml2 slo redirect profile.
     */
    private Saml2SloProfileRedirect sloRedirect = new Saml2SloProfileRedirect();

    @RequiresModule(name = "cas-server-support-saml-idp")
    @Getter
    @Setter
    public static class Saml2SsoProfileRedirect implements Serializable {
        private static final long serialVersionUID = 8976431439191949383L;
    }

    @RequiresModule(name = "cas-server-support-saml-idp")
    @Getter
    @Setter
    public static class Saml2SloProfileRedirect implements Serializable {
        private static final long serialVersionUID = 1976431439191949383L;

        /**
         * Whether the initial request should be explicitly url-decoded
         * before it's consumed by the decoder.
         */
        private boolean urlDecodeRequest;
    }

    @RequiresModule(name = "cas-server-support-saml-idp")
    @Getter
    @Setter
    public static class Saml2SsoProfilePost implements Serializable {
        private static final long serialVersionUID = 6576431439191949383L;

        /**
         * Whether the initial request should be explicitly url-decoded
         * before it's consumed by the decoder.
         */
        private boolean urlDecodeRequest;
    }

    @RequiresModule(name = "cas-server-support-saml-idp")
    @Getter
    @Setter
    public static class Saml2SsoProfilePostSimpleSign implements Serializable {
        private static final long serialVersionUID = 2276431439191949383L;

        /**
         * Whether the initial request should be explicitly url-decoded
         * before it's consumed by the decoder.
         */
        private boolean urlDecodeRequest;
    }
}
