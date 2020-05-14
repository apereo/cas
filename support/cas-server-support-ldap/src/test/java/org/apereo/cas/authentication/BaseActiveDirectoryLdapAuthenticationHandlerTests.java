package org.apereo.cas.authentication;


import org.junit.jupiter.api.Tag;

/**
 * Base class for Active Directory Ldap Unit tests for {@link LdapAuthenticationHandler}.
 *
 * @author Hal Deadman
 * @since 6.1.0
 */
@Tag("Ldap")
public abstract class BaseActiveDirectoryLdapAuthenticationHandlerTests extends BaseLdapAuthenticationHandlerTests{

    public static final String AD_TRUST_STORE = "file:/tmp/adcacerts.jks";

    public static final String AD_ADMIN_PASSWORD = "M3110nM3110n#1";

    public static final String AD_LDAP_URL = "ldap://localhost:10390";

    @Override
    protected String getSuccessPassword() {
        return "P@ssw0rd";
    }

    @Override
    protected String[] getPrincipalAttributes() {
        return new String[] {"cn", "sAMAccountName"};
    }

}
