/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.support.spnego.authentication.handler.support;

import java.net.URL;

import jcifs.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Configuration helper for JCIFS and the Spring framework.
 *
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @author Scott Battaglia
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

    private static final String JCIFS_PROP_DOMAIN_CONTROLLER = "jcifs.http.domainController";

    private static final String JCIFS_PROP_NETBIOS_WINS = "jcifs.netbios.wins";

    private static final String JCIFS_PROP_CLIENT_DOMAIN = "jcifs.smb.client.domain";

    private static final String JCIFS_PROP_CLIENT_USERNAME = "jcifs.smb.client.username";

    private static final String JCIFS_PROP_CLIENT_PASSWORD = "jcifs.smb.client.password";

    /**
     * -- the service principal you just created. Using the previous example,
     * this would be "HTTP/mybox at DOMAIN.COM".
     */
    private static final String JCIFS_PROP_SERVICE_PRINCIPAL = "jcifs.spnego.servicePrincipal";

    /**
     * The password for the service principal account, required only if you
     * decide not to use keytab.
     */
    private static final String JCIFS_PROP_SERVICE_PASSWORD = "jcifs.spnego.servicePassword";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String loginConf;


    public JCIFSConfig() {
        Config.setProperty("jcifs.smb.client.soTimeout", "300000");
        Config.setProperty("jcifs.netbios.cachePolicy", "600");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (System.getProperty(SYS_PROP_LOGIN_CONF) != null) {
            logger.warn("found login config in system property, may overide : "
                    + System.getProperty(SYS_PROP_LOGIN_CONF));
        }

        URL url = getClass().getResource(
                this.loginConf == null ? DEFAULT_LOGIN_CONFIG : this.loginConf);
        if (url != null) {
            this.loginConf = url.toExternalForm();
        }
        if (this.loginConf != null) {
            System.setProperty(SYS_PROP_LOGIN_CONF, this.loginConf);
        } else {
            url = getClass().getResource("/jcifs/http/login.conf");
            if (url != null) {
                System.setProperty(SYS_PROP_LOGIN_CONF, url.toExternalForm());
            }
        }
        logger.debug("configured login configuration path : "
                + System.getProperty(SYS_PROP_LOGIN_CONF));
    }

    public void setJcifsServicePassword(final String jcifsServicePassword) {
        logger.debug("jcifsServicePassword is set to *****");
        Config.setProperty(JCIFS_PROP_SERVICE_PASSWORD, jcifsServicePassword);
    }

    public void setJcifsServicePrincipal(final String jcifsServicePrincipal) {
        logger.debug("jcifsServicePrincipal is set to {}", jcifsServicePrincipal);
        Config.setProperty(JCIFS_PROP_SERVICE_PRINCIPAL, jcifsServicePrincipal);
    }

    public void setKerberosConf(final String kerberosConf) {
        logger.debug("kerberosConf is set to :{}", kerberosConf);
        System.setProperty(SYS_PROP_KERBEROS_CONF, kerberosConf);
    }

    public void setKerberosKdc(final String kerberosKdc) {
        logger.debug("kerberosKdc is set to : {}", kerberosKdc);
        System.setProperty(SYS_PROP_KERBEROS_KDC, kerberosKdc);
    }

    public void setKerberosRealm(final String kerberosRealm) {
        logger.debug("kerberosRealm is set to :{}", kerberosRealm);
        System.setProperty(SYS_PROP_KERBEROS_REALM, kerberosRealm);
    }

    public void setLoginConf(final String loginConf) {
        this.loginConf = loginConf;
    }

    public void setUseSubjectCredsOnly(final boolean useSubjectCredsOnly) {
        logger.debug("useSubjectCredsOnly is set to {}", useSubjectCredsOnly);
        System.setProperty(SYS_PROP_USE_SUBJECT_CRED_ONLY, Boolean.toString(useSubjectCredsOnly));
    }

    public void setKerberosDebug(final String kerberosDebug) {
        logger.debug("kerberosDebug is set to : {}", kerberosDebug);
        System.setProperty(SYS_PROP_KERBEROS_DEBUG, kerberosDebug);
    }

    /**
     * @param jcifsDomain the jcifsDomain to set
     */
    public void setJcifsDomain(final String jcifsDomain) {
        logger.debug("jcifsDomain is set to {}", jcifsDomain);
        Config.setProperty(JCIFS_PROP_CLIENT_DOMAIN, jcifsDomain);
    }

    /**
     * @param jcifsDomainController the jcifsDomainController to set
     */
    public void setJcifsDomainController(final String jcifsDomainController) {
        logger.debug("jcifsDomainController is set to {}", jcifsDomainController);
        Config.setProperty(JCIFS_PROP_DOMAIN_CONTROLLER, jcifsDomainController);
    }

    /**
     * @param jcifsPassword the jcifsPassword to set
     */
    public void setJcifsPassword(final String jcifsPassword) {
        Config.setProperty(JCIFS_PROP_CLIENT_PASSWORD, jcifsPassword);
        logger.debug("jcifsPassword is set to *****");
    }

    /**
     * @param jcifsUsername the jcifsUsername to set
     */
    public void setJcifsUsername(final String jcifsUsername) {
        logger.debug("jcifsUsername is set to {}", jcifsUsername);
        Config.setProperty(JCIFS_PROP_CLIENT_USERNAME, jcifsUsername);
    }

    /**
     * @param jcifsNetbiosWins the jcifsNetbiosWins to set
     */
    public void setJcifsNetbiosWins(final String jcifsNetbiosWins) {
        logger.debug("jcifsNetbiosWins is set to {}", jcifsNetbiosWins);
        Config.setProperty(JCIFS_PROP_NETBIOS_WINS, jcifsNetbiosWins);
    }
}
