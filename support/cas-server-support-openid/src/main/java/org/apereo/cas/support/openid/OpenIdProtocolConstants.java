package org.apereo.cas.support.openid;

import lombok.experimental.UtilityClass;

/**
 * OpenID constants.
 *
 * @author Jerome Leleu
 * @deprecated 6.2
 * @since 4.1.0
 */
@UtilityClass
@Deprecated(since = "6.2.0")
public class OpenIdProtocolConstants {
    /**
     * The application callback url.
     */
    public static final String OPENID_RETURNTO = "openid.return_to";

    /**
     * The OpenID association handle.
     */
    public static final String OPENID_ASSOCHANDLE = "openid.assoc_handle";

    /**
     * The OpenID mode.
     */
    public static final String OPENID_MODE = "openid.mode";

    /**
     * The OpenID cancel mode.
     */
    public static final String CANCEL = "cancel";

    /**
     * The OpenID identity.
     */
    public static final String OPENID_IDENTITY = "openid.identity";

    /**
     * The OpenID SIG.
     */
    public static final String OPENID_SIG = "openid.sig";

    /**
     * When the user can select its own username for login.
     */
    public static final String OPENID_IDENTIFIERSELECT = "http://specs.openid.net/auth/2.0/identifier_select";

    /**
     * The OpenID associate mode.
     */
    public static final String ASSOCIATE = "associate";

    /**
     * Check authentication constant.
     */
    public static final String CHECK_AUTHENTICATION = "check_authentication";

}
