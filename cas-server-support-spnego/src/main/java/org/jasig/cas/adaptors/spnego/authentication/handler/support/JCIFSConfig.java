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
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class JCIFSConfig implements InitializingBean {

    private static final String DEFAULT_LOGIN_CONFIG = "/login.conf";

    private static final String SYS_PROP_USE_SUBJECT_CRED_ONLY = "javax.security.auth.useSubjectCredsOnly";

    private static final String SYS_PROP_LOGIN_CONF = "java.security.auth.login.config";

    private static final String SYS_PROP_KERBEROS_DEBUG = "sun.security.krb5.debug";

    private static final String SYS_PROP_KERBEROS_CONF = "java.security.krb5.conf";

    private static final String SYS_PROP_KERBEROS_REALM = "java.security.krb5.realm";

    private static final String SYS_PROP_KERBEROS_KDC = "java.security.krb5.kdc";

    /**
     * -- the service principal you just created. Using the previous example,
     * this would be "HTTP/mybox at DOMAIN.COM".
     */
    private static final String JCIFS_PROP_SERVICE_PRINCIPAL = "jcifs.spnego.servicePrincipal";

    /**
     * The password for the service principal account, required only if you decide not to use
     * keytab.
     */
    private static final String JCIFS_PROP_SERVICE_PASSWORD = "jcifs.spnego.servicePassword";

    private Log log = LogFactory.getLog(this.getClass());

    private String useSubjectCredsOnly;

    private String loginConf;

    private String kerberosConf;

    private String kerberosRealm;

    private String kerberosDebug;

    private String kerberosKdc;

    private String jcifsServicePrincipal;

    private String jcifsServicePassword;

    public void afterPropertiesSet() throws Exception {
        // login config
        log.debug("initializing JCIFS config");

        Config.setProperty("jcifs.smb.client.soTimeout", "300000");
        Config.setProperty("jcifs.netbios.cachePolicy", "600");

        log.debug("jcifsServicePrincipal is set to "
            + this.jcifsServicePrincipal);
        Config.setProperty(JCIFS_PROP_SERVICE_PRINCIPAL,
            this.jcifsServicePrincipal);
        if(this.jcifsServicePassword!=null){
        log.debug("jcifsServicePassword is set to *****");
        Config.setProperty(JCIFS_PROP_SERVICE_PASSWORD,
            this.jcifsServicePassword);
        }else{
        	log.debug("jcifsServicePassword is null, skipping");
        }
        if (this.kerberosRealm != null) {
            log.debug("kerberosRealm is set to :" + this.kerberosRealm);
            System.setProperty(SYS_PROP_KERBEROS_REALM, this.kerberosRealm);
        }
        if (this.kerberosKdc != null) {
            log.debug("kerberosKdc is set to : " + this.kerberosKdc);
            System.setProperty(SYS_PROP_KERBEROS_KDC, this.kerberosKdc);
        }
        if (this.kerberosConf != null) {
            log.debug("kerberosConf is set to :" + this.kerberosConf);
            System.setProperty(SYS_PROP_KERBEROS_CONF, this.kerberosConf);
        }
        if (this.kerberosDebug != null) {
            log.debug("kerberosDebug is set to : " + this.kerberosDebug);
            System.setProperty(SYS_PROP_KERBEROS_DEBUG, this.kerberosDebug);
        }

        if (System.getProperty(SYS_PROP_LOGIN_CONF) != null) {
            log.warn("found login config in system property, may overide : "
                + System.getProperty(SYS_PROP_LOGIN_CONF));
        }
        URL url = getClass().getResource(
            (this.loginConf == null) ? DEFAULT_LOGIN_CONFIG : this.loginConf);
        if (url != null)
            this.loginConf = url.toExternalForm();
        if (this.loginConf != null) {
            System.setProperty(SYS_PROP_LOGIN_CONF, this.loginConf);
        } else {
            url = getClass().getResource("/jcifs/http/login.conf");
            if (url != null) {
                System.setProperty(SYS_PROP_LOGIN_CONF, url.toExternalForm());
            }
        }
        log.debug("configured login configuration path : "
            + System.getProperty(SYS_PROP_LOGIN_CONF));

        if ("true".equalsIgnoreCase(this.useSubjectCredsOnly)) {
            log.debug("useSubjectCredsOnly is set to true");
            System.setProperty(SYS_PROP_USE_SUBJECT_CRED_ONLY, "true");
        } else if ("false".equalsIgnoreCase(this.useSubjectCredsOnly)) {
            log.debug("useSubjectCredsOnly is set to false");
            System.setProperty(SYS_PROP_USE_SUBJECT_CRED_ONLY, "false");
        }

    }

    public String getJcifsServicePassword() {
        return this.jcifsServicePassword;
    }

    public void setJcifsServicePassword(final String jcifsServicePassword) {
        this.jcifsServicePassword = jcifsServicePassword;
    }

    public String getJcifsServicePrincipal() {
        return this.jcifsServicePrincipal;
    }

    public void setJcifsServicePrincipal(final String jcifsServicePrincipal) {
        this.jcifsServicePrincipal = jcifsServicePrincipal;
    }

    public String getKerberosConf() {
        return this.kerberosConf;
    }

    public void setKerberosConf(final String kerberosConf) {
        this.kerberosConf = kerberosConf;
    }

    public String getKerberosKdc() {
        return this.kerberosKdc;
    }

    public void setKerberosKdc(final String kerberosKdc) {
        this.kerberosKdc = kerberosKdc;
    }

    public String getKerberosRealm() {
        return this.kerberosRealm;
    }

    public void setKerberosRealm(final String kerberosRealm) {
        this.kerberosRealm = kerberosRealm;
    }

    public String getLoginConf() {
        return this.loginConf;
    }

    public void setLoginConf(final String loginConf) {
        this.loginConf = loginConf;
    }

    public String getUseSubjectCredsOnly() {
        return this.useSubjectCredsOnly;
    }

    public void setUseSubjectCredsOnly(final String useSubjectCredsOnly) {
        this.useSubjectCredsOnly = useSubjectCredsOnly;
    }

    public String getKerberosDebug() {
        return this.kerberosDebug;
    }

    public void setKerberosDebug(final String kerberosDebug) {
        this.kerberosDebug = kerberosDebug;
    }
}
