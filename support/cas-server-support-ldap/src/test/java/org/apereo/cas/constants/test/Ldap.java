package org.apereo.cas.constants.test;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;

/**
 * This is {@link Ldap}. Constants for LDAP test classes.
 *
 * @author Timur Duehr
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class Ldap {
    @Autowired
    private static Environment ENV;

    public static String getManagerDn() {
        return ENV.getProperty("ldap.managerDn");
    }

    public static int getPort() {
        return Integer.parseInt(ENV.getProperty("ldap.port","0"));
    }

    public static String getBindDn() {
        return ENV.getProperty("ldap.bindDn");
    }

    public static String getPeopleDn() {
        return ENV.getProperty("ldap.peopleDn");
    }

    public static String getBindPass() {
        return ENV.getProperty("ldap.bindPassword");
    }

    public static String getBaseDn() {
        return ENV.getProperty("ldap.baseDn");
    }
    public static String getHost() {
        return ENV.getProperty("ldap.host");
    }

    public static String getUrl() {
        return ENV.getProperty("ldap.url");
    }
}
