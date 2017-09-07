package org.apereo.cas.ws.idp;

import org.apache.commons.lang3.EnumUtils;
import org.apereo.cas.util.CollectionUtils;

import java.util.List;

/**
 * This is {@link WSFederationClaims}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public enum WSFederationClaims {
    /**
     * Claims email address 2005 claim type constants.
     */
    EMAIL_ADDRESS_2005("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress"),
    /**
     * Claims email address claim type constants.
     */
    EMAIL_ADDRESS("http://schemas.xmlsoap.org/claims/EmailAddress"),
    /**
     * Claims given name claim type constants.
     */
    GIVEN_NAME("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname"),
    /**
     * Claims name claim type constants.
     */
    NAME("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"),
    /**
     * Claims user principal name 2005 claim type constants.
     */
    USER_PRINCIPAL_NAME_2005("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/upn"),
    /**
     * Claims user principal name claim type constants.
     */
    USER_PRINCIPAL_NAME("http://schemas.xmlsoap.org/claims/UPN"),
    /**
     * Claims common name claim type constants.
     */
    COMMON_NAME("http://schemas.xmlsoap.org/claims/CommonName"),
    /**
     * Claims group claim type constants.
     */
    GROUP("http://schemas.xmlsoap.org/claims/Group"),
    /**
     * Claims ms role claim type constants.
     */
    MS_ROLE("http://schemas.microsoft.com/ws/2008/06/identity/claims/role"),
    /**
     * Claims role claim type constants.
     */
    ROLE("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role"),
    /**
     * Claims surname claim type constants.
     */
    SURNAME("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname"),
    /**
     * Claims private id claim type constants.
     */
    PRIVATE_ID("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/privatepersonalidentifier"),
    /**
     * Claims name identifier claim type constants.
     */
    NAME_IDENTIFIER("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier"),
    /**
     * Claims authentication method claim type constants.
     */
    AUTHENTICATION_METHOD("http://schemas.microsoft.com/ws/2008/06/identity/claims/authenticationmethod"),
    /**
     * Claims deny only group sid claim type constants.
     */
    DENY_ONLY_GROUP_SID("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/denyonlysid"),
    /**
     * Claims deny only primary sid claim type constants.
     */
    DENY_ONLY_PRIMARY_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/denyonlyprimarysid"),
    /**
     * Claims deny only primary group sid claim type constants.
     */
    DENY_ONLY_PRIMARY_GROUP_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/denyonlyprimarygroupsid"),
    /**
     * Claims group sid claim type constants.
     */
    GROUP_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/groupsid"),
    /**
     * Claims primary group sid claim type constants.
     */
    PRIMARY_GROUP_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/primarygroupsid"),
    /**
     * Claims primary sid claim type constants.
     */
    PRIMARY_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/primarysid"),
    /**
     * Claims windows account name claim type constants.
     */
    WINDOWS_ACCOUNT_NAME("http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname"),
    /**
     * Claims puid claim type constants.
     */
    PUID("http://schemas.xmlsoap.org/claims/PUID");

    /**
     * All claims.
     */
    public static final List<WSFederationClaims> ALL_CLAIMS =
            CollectionUtils.wrapList(EMAIL_ADDRESS_2005,
                    EMAIL_ADDRESS,
                    GIVEN_NAME,
                    NAME,
                    USER_PRINCIPAL_NAME_2005,
                    USER_PRINCIPAL_NAME,
                    COMMON_NAME,
                    GROUP,
                    ROLE,
                    MS_ROLE,
                    SURNAME,
                    PRIVATE_ID,
                    NAME_IDENTIFIER,
                    AUTHENTICATION_METHOD,
                    DENY_ONLY_GROUP_SID,
                    DENY_ONLY_PRIMARY_SID,
                    DENY_ONLY_PRIMARY_GROUP_SID,
                    GROUP_SID,
                    PRIMARY_GROUP_SID,
                    PRIMARY_SID,
                    WINDOWS_ACCOUNT_NAME,
                    PUID);

    /**
     * The Uri.
     */
    private final String uri;

    /**
     * Instantiates a new Claim type constants.
     *
     * @param str the str
     */
    WSFederationClaims(final String str) {
        this.uri = str;
    }

    /**
     * Gets uri.
     *
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Contains claim.
     *
     * @param claimName the claim name
     * @return true/false
     */
    public static boolean contains(final String claimName) {
        return EnumUtils.isValidEnum(WSFederationClaims.class, claimName);
    }
}
