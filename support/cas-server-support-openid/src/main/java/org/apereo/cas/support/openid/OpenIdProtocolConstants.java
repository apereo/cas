package org.apereo.cas.support.openid;

/**
 * OpenID constants.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 * @deprecated 6.2
 */
@Deprecated(since = "6.2.0")
public interface OpenIdProtocolConstants {
    /**
     * The application callback url.
     */
    String OPENID_RETURNTO = "openid.return_to";

    /**
     * The OpenID association handle.
     */
    String OPENID_ASSOCHANDLE = "openid.assoc_handle";

    /**
     * The OpenID mode.
     */
    String OPENID_MODE = "openid.mode";

    /**
     * The OpenID cancel mode.
     */
    String CANCEL = "cancel";

    /**
     * The OpenID identity.
     */
    String OPENID_IDENTITY = "openid.identity";

    /**
     * The OpenID SIG.
     */
    String OPENID_SIG = "openid.sig";

    /**
     * When the user can select its own username for login.
     */
    String OPENID_IDENTIFIERSELECT = "http://specs.openid.net/auth/2.0/identifier_select";

    /**
     * The OpenID associate mode.
     */
    String ASSOCIATE = "associate";

    /**
     * Check authentication constant.
     */
    String CHECK_AUTHENTICATION = "check_authentication";

}
