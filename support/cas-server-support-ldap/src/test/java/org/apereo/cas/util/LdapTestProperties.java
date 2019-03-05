package org.apereo.cas.util;

import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.env.Environment;

/**
 * This is {@link LdapTestProperties}.
 *
 * @author Timur Duehr
 * @since 6.1.0
 */
@UtilityClass
public class LdapTestProperties {
    public static String host() {
        return getEnvironment().getProperty("ldap.host", "localhost");
    }

    public static Environment getEnvironment() {
        return ApplicationContextProvider.getApplicationContext().getEnvironment();
    }

    public static int port() {
        return Integer.parseInt(getEnvironment().getProperty("ldap.port", Strings.EMPTY));
    }

    public static String bindDn() {
        return getEnvironment().getProperty("ldap.bindDn");
    }

    public static String bindPass() {
        return getEnvironment().getProperty("ldap.bindPass");
    }

    public static String baseDn() {
        return getEnvironment().getProperty("ldap.baseDn");
    }

    public static String peopleDn() {
        return "ou=people," + getEnvironment().getProperty("ldap.baseDn");
    }

}
