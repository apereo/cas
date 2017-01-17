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

    CLAIMS_EMAIL_ADDRESS_2005("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress"),
    CLAIMS_EMAIL_ADDRESS("http://schemas.xmlsoap.org/claims/EmailAddress"),
    CLAIMS_GIVEN_NAME("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname"),
    CLAIMS_NAME("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"),
    CLAIMS_USER_PRINCIPAL_NAME_2005("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/upn"),
    CLAIMS_USER_PRINCIPAL_NAME("http://schemas.xmlsoap.org/claims/UPN"),
    CLAIMS_COMMON_NAME("http://schemas.xmlsoap.org/claims/CommonName"),
    CLAIMS_GROUP("http://schemas.xmlsoap.org/claims/Group"),
    CLAIMS_MS_ROLE("http://schemas.microsoft.com/ws/2008/06/identity/claims/role"),
    CLAIMS_ROLE("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role"),
    CLAIMS_SURNAME("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname"),
    CLAIMS_PRIVATE_ID("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/privatepersonalidentifier"),
    CLAIMS_NAME_IDENTIFIER("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier"),
    CLAIMS_AUTHENTICATION_METHOD("http://schemas.microsoft.com/ws/2008/06/identity/claims/authenticationmethod"),
    CLAIMS_DENY_ONLY_GROUP_SID("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/denyonlysid"),
    CLAIMS_DENY_ONLY_PRIMARY_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/denyonlyprimarysid"),
    CLAIMS_DENY_ONLY_PRIMARY_GROUP_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/denyonlyprimarygroupsid"),
    CLAIMS_GROUP_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/groupsid"),
    CLAIMS_PRIMARY_GROUP_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/primarygroupsid"),
    CLAIMS_PRIMARY_SID("http://schemas.microsoft.com/ws/2008/06/identity/claims/primarysid"),
    CLAIMS_WINDOWS_ACCOUNT_NAME("http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname"),
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

    private String uri;

    ClaimTypeConstants(final String str) {
        this.uri = str;
    }

    public String getUri() {
        return uri;
    }
}
