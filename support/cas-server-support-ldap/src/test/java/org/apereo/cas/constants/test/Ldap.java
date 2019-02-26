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

    public static String URL = ENV.getProperty("ldap.url");
    public static String MANAGER_DN = ENV.getProperty("ldap.managerDn");
    public static int PORT = Integer.parseInt(ENV.getProperty("ldap.port", "0"));
    public static String BIND_DN = ENV.getProperty("ldap.bindDn");
    public static String PEOPLE_DN = ENV.getProperty("ldap.peopleDn");
    public static String BIND_PASS = ENV.getProperty("ldap.bindPassword");
    public static String BASE_DN = ENV.getProperty("ldap.baseDn");
    public static String HOST = ENV.getProperty("ldap.host");

}
