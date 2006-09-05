/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.spnego.authentication.handler.support;

import java.net.URL;

import jcifs.Config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Configuration helper for JCIFS and the Spring framework.
 * 
 * 
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @version $Id$
 * @since 3.1
 */
public class JCIFSConfig implements InitializingBean {

    private static final String DEFAULT_LOGIN_CONFIG = "/WEB-INF/login.conf";

    private static final String SYS_PROP_USE_SUBJECT_CRED_ONLY = "javax.security.auth.useSubjectCredsOnly";

    private static final String SYS_PROP_LOGIN_CONF = "java.security.auth.login.config";

    private static final String SYS_PROP_KERBEROS_DEBUG = "sun.security.krb5.debug";

    private static final String SYS_PROP_KERBEROS_CONF = "java.security.krb5.conf";

    private static final String SYS_PROP_KERBEROS_REALM = "java.security.krb5.realm";

    private static final String SYS_PROP_KERBEROS_KDC = "java.security.krb5.kdc";

    /**
     * -- Set this to true, otherwise only NTLM will be advertised/supported.
     */
    private static final String JCIFS_PROP_ENABLE_KERBEROS = "jcifs.http.enableNegotiate";

    /**
     * -- the service principal you just created. Using the previous example, this would be
     * "HTTP/mybox at DOMAIN.COM".
     */
    private static final String JCIFS_PROP_SERVICE_PRINCIPAL = "jcifs.spnego.servicePrincipal";

    /**
     * The password for the service principal account (same password as before).
     */
    private static final String JCIFS_PROP_SERVICE_PASSWORD = "jcifs.spnego.servicePassword";

    private Log log = LogFactory.getLog(this.getClass());

    private String useSubjectCredsOnly;

    private String loginConf;

    private String kerberosConf;

    private String kerberosRealm;

    private String kerberosDebug;

    private String kerberosKdc;

    private String jcifsKerberosEnable;

    private String jcifsServicePrincipal;

    private String jcifsServicePassword;

    public void afterPropertiesSet() throws Exception {
        // login config
        log.debug("initializing JCIFS config");

        Config.setProperty("jcifs.smb.client.soTimeout", "300000");
        Config.setProperty("jcifs.netbios.cachePolicy", "600");

        log.debug("jcifsServicePrincipal is set to " + jcifsServicePrincipal);
        Config.setProperty(JCIFS_PROP_SERVICE_PRINCIPAL, jcifsServicePrincipal);
        log.debug("jcifsServicePassword is set to *****");
        Config.setProperty(JCIFS_PROP_SERVICE_PASSWORD, jcifsServicePassword);

        if (kerberosRealm != null) {
            log.debug("kerberosRealm is set to :" + kerberosRealm);
            System.setProperty(SYS_PROP_KERBEROS_REALM, kerberosRealm);
        }
        if (kerberosKdc != null) {
            log.debug("kerberosKdc is set to : " + kerberosKdc);
            System.setProperty(SYS_PROP_KERBEROS_KDC, kerberosKdc);
        }
        if (kerberosConf != null) {
            log.debug("kerberosConf is set to :" + kerberosConf);
            System.setProperty(SYS_PROP_KERBEROS_CONF, kerberosConf);
        }
        if (kerberosDebug != null) {
            log.debug("kerberosDebug is set to : " + kerberosDebug);
            System.setProperty(SYS_PROP_KERBEROS_DEBUG, kerberosDebug);
        }

        if ("true".equalsIgnoreCase(jcifsKerberosEnable)) {
            log.debug("jcifsKerberosEnable is set to true");
            Config.setProperty(JCIFS_PROP_ENABLE_KERBEROS, "true");

        } else if ("false".equalsIgnoreCase(jcifsKerberosEnable)) {
            log.debug("jcifsKerberosEnable is set to false");
            Config.setProperty(JCIFS_PROP_ENABLE_KERBEROS, "false");
        }

        if (System.getProperty(SYS_PROP_LOGIN_CONF) != null) {
            log.warn("found login config in system property, may overide : "
                    + System.getProperty(SYS_PROP_LOGIN_CONF));
        }
        URL url = getClass().getResource((loginConf == null) ? DEFAULT_LOGIN_CONFIG : loginConf);
        if (url != null)
            loginConf = url.toExternalForm();
        if (loginConf != null) {
            System.setProperty(SYS_PROP_LOGIN_CONF, loginConf);
        } else {
            url = getClass().getResource("/jcifs/http/login.conf");
            if (url != null) {
                System.setProperty(SYS_PROP_LOGIN_CONF, url.toExternalForm());
            }
        }
        log.debug("configured login configuration path : "
                + System.getProperty(SYS_PROP_LOGIN_CONF));

        if ("true".equalsIgnoreCase(useSubjectCredsOnly)) {
            log.debug("useSubjectCredsOnly is set to true");
            System.setProperty(SYS_PROP_USE_SUBJECT_CRED_ONLY, "true");
        } else if ("false".equalsIgnoreCase(useSubjectCredsOnly)) {
            log.debug("useSubjectCredsOnly is set to false");
            System.setProperty(SYS_PROP_USE_SUBJECT_CRED_ONLY, "false");
        }

    }

    public String getJcifsKerberosEnable() {
        return jcifsKerberosEnable;
    }

    public void setJcifsKerberosEnable(String jcifsKerberosEnable) {
        this.jcifsKerberosEnable = jcifsKerberosEnable;
    }

    public String getJcifsServicePassword() {
        return jcifsServicePassword;
    }

    public void setJcifsServicePassword(String jcifsServicePassword) {
        this.jcifsServicePassword = jcifsServicePassword;
    }

    public String getJcifsServicePrincipal() {
        return jcifsServicePrincipal;
    }

    public void setJcifsServicePrincipal(String jcifsServicePrincipal) {
        this.jcifsServicePrincipal = jcifsServicePrincipal;
    }

    public String getKerberosConf() {
        return kerberosConf;
    }

    public void setKerberosConf(String kerberosConf) {
        this.kerberosConf = kerberosConf;
    }

    public String getKerberosKdc() {
        return kerberosKdc;
    }

    public void setKerberosKdc(String kerberosKdc) {
        this.kerberosKdc = kerberosKdc;
    }

    public String getKerberosRealm() {
        return kerberosRealm;
    }

    public void setKerberosRealm(String kerberosRealm) {
        this.kerberosRealm = kerberosRealm;
    }

    public String getLoginConf() {
        return loginConf;
    }

    public void setLoginConf(String loginConf) {
        this.loginConf = loginConf;
    }

    public String getUseSubjectCredsOnly() {
        return useSubjectCredsOnly;
    }

    public void setUseSubjectCredsOnly(String useSubjectCredsOnly) {
        this.useSubjectCredsOnly = useSubjectCredsOnly;
    }

    public String getKerberosDebug() {
        return kerberosDebug;
    }

    public void setKerberosDebug(String kerberosDebug) {
        this.kerberosDebug = kerberosDebug;
    }

}
