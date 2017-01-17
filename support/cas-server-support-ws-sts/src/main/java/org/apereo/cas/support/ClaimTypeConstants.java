package org.apereo.cas.support;

import java.util.Arrays;
import java.util.List;

/**
 * This is {@link ClaimTypeConstants}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public enum ClaimTypeConstants {

    /**
     * Claims email address 2005 claim type constants.
     */
    CLAIMS_EMAIL_ADDRESS_2005("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress"),
    /**
     * Claims email address claim type constants.
     */
    CLAIMS_EMAIL_ADDRESS("http://schemas.xmlsoap.org/claims/EmailAddress"),
    /**
     * Claims given name claim type constants.
     */
    CLAIMS_GIVEN_NAME("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname"),
    /**
     * Claims name claim type constants.
     */
    CLAIMS_NAME("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"),
    /**
     * Claims user principal name 2005 claim type constants.
     */
    CLAIMS_USER_PRINCIPAL_NAME_2005("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/upn"),
    /**
     * Claims user principal name claim type constants.
     */
    CLAIMS_USER_PRINCIPAL_NAME("http://schemas.xmlsoap.org/claims/UPN"),
    /**
     * Claims common name claim type constants.
     */
    CLAIMS_COMMON_NAME("http://schemas.xmlsoap.org/claims/CommonName"),
    /**
     * Claims group claim type constants.
     */
    CLAIMS_GROUP("http://schemas.xmlsoap.org/claims/Group"),
    /**
     * Claims ms role claim type constants.
     */
    CLAIMS_MS_ROLE("http://schemas.microsoft.com/ws/2008/06/identity/claims/role"),
    /**
     * Claims role claim type constants.
     */
    CLAIMS_ROLE("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role"),
    /**
     * Claims surname claim type constants.
     */
    CLAIMS_SURNAME("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname"),
    /**
     * Claims private id claim type constants.
     */
    CLAIMS_PRIVATE_ID("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/privatepersonalidentifier"),
    /**
     * Claims name identifier claim type constants.
     */
    CLAIMS_NAME_IDENTIFIER("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier"),
    /**
     * Claims authentication method claim type constants.
     */
    CLAIMS_AUTHENTICATION_METHOD("http://schemas.microsoft.com/ws/2008/06/identity/claims/authenticationmethod"),
    /**
     * Claims deny only group sid claim type constants.
     */
    CLAIMS_DENY_ONLY_GROUP_SID("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/denyonlysid"),
    /**
     * Claims deny only primary sid claim type constants.
     */
    CLAIMS_DENY_ONLY_PRIMARY_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/denyonlyprimarysid"),
    /**
     * Claims deny only primary group sid claim type constants.
     */
    CLAIMS_DENY_ONLY_PRIMARY_GROUP_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/denyonlyprimarygroupsid"),
    /**
     * Claims group sid claim type constants.
     */
    CLAIMS_GROUP_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/groupsid"),
    /**
     * Claims primary group sid claim type constants.
     */
    CLAIMS_PRIMARY_GROUP_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/primarygroupsid"),
    /**
     * Claims primary sid claim type constants.
     */
    CLAIMS_PRIMARY_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/primarysid"),
    /**
     * Claims windows account name claim type constants.
     */
    CLAIMS_WINDOWS_ACCOUNT_NAME("http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname"),
    /**
     * Claims puid claim type constants.
     */
    CLAIMS_PUID("http://schemas.xmlsoap.org/claims/PUID");

    /**
     * All claims.
     */
    public static final List<ClaimTypeConstants> ALL_CLAIMS =
            Arrays.asList(CLAIMS_EMAIL_ADDRESS_2005,
                    CLAIMS_EMAIL_ADDRESS,
                    CLAIMS_GIVEN_NAME,
                    CLAIMS_NAME,
                    CLAIMS_USER_PRINCIPAL_NAME_2005,
                    CLAIMS_USER_PRINCIPAL_NAME,
                    CLAIMS_COMMON_NAME,
                    CLAIMS_GROUP,
                    CLAIMS_ROLE,
                    CLAIMS_MS_ROLE,
                    CLAIMS_SURNAME,
                    CLAIMS_PRIVATE_ID,
                    CLAIMS_NAME_IDENTIFIER,
                    CLAIMS_AUTHENTICATION_METHOD,
                    CLAIMS_DENY_ONLY_GROUP_SID,
                    CLAIMS_DENY_ONLY_PRIMARY_SID,
                    CLAIMS_DENY_ONLY_PRIMARY_GROUP_SID,
                    CLAIMS_GROUP_SID,
                    CLAIMS_PRIMARY_GROUP_SID,
                    CLAIMS_PRIMARY_SID,
                    CLAIMS_WINDOWS_ACCOUNT_NAME,
                    CLAIMS_PUID);

    /**
     * The Uri.
     */
    private String uri;

    /**
     * Instantiates a new Claim type constants.
     *
     * @param str the str
     */
    ClaimTypeConstants(final String str) {
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
}
