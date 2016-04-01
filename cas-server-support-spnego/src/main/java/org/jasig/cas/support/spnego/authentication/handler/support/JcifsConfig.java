package org.jasig.cas.support.spnego.authentication.handler.support;

import jcifs.Config;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.net.URL;

/**
 * Configuration helper for JCIFS and the Spring framework.
 *
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @author Scott Battaglia
 * @since 4.2.0
 */
public final class JcifsConfig implements InitializingBean {

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

    private static final String JCIFS_PROP_CLIENT_SOTIMEOUT = "jcifs.smb.client.soTimeout";

    private static final String JCIFS_PROP_NETBIOS_CACHE_POLICY = "jcifs.netbios.cachePolicy";

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

    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private String loginConf;


    /**
     * Instantiates a new jCIFS config.
     */
    public JcifsConfig() {
        Config.setProperty(JCIFS_PROP_CLIENT_SOTIMEOUT, "300000");
        Config.setProperty(JCIFS_PROP_NETBIOS_CACHE_POLICY, "600");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final String propValue = System.getProperty(SYS_PROP_LOGIN_CONF);
        if (propValue != null) {
            logger.warn("found login config in system property, may override : {}", propValue);
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
        logger.debug("configured login configuration path : {}", propValue);
    }


    /**
     * Sets the jcifs service password.
     *
     * @param jcifsServicePassword the new jcifs service password
     */
    @Autowired
    public void setJcifsServicePassword(@Value("${cas.spnego.jcifs.service.password:}")
                                        final String jcifsServicePassword) {
        if (StringUtils.isNotBlank(jcifsServicePassword)) {
            logger.debug("jcifsServicePassword is set to *****");
            Config.setProperty(JCIFS_PROP_SERVICE_PASSWORD, jcifsServicePassword);
        }
    }


    /**
     * Sets the jcifs service principal.
     *
     * @param jcifsServicePrincipal the new jcifs service principal
     */
    @Autowired
    public void setJcifsServicePrincipal(@Value("${cas.spnego.service.principal:HTTP/cas.example.com@EXAMPLE.COM}")
                                         final String jcifsServicePrincipal) {
        if (StringUtils.isNotBlank(jcifsServicePrincipal)) {
            logger.debug("jcifsServicePrincipal is set to {}", jcifsServicePrincipal);
            Config.setProperty(JCIFS_PROP_SERVICE_PRINCIPAL, jcifsServicePrincipal);
        }
    }

    /**
     * Sets the kerberos conf.
     *
     * @param kerberosConf the new kerberos conf
     */
    @Autowired
    public void setKerberosConf(@Value("${cas.spnego.kerb.conf:}") final String kerberosConf) {
        if (StringUtils.isNotBlank(kerberosConf)) {

            logger.debug("kerberosConf is set to :{}", kerberosConf);
            System.setProperty(SYS_PROP_KERBEROS_CONF, kerberosConf);
        }
    }

    /**
     * Sets the kerberos kdc.
     *
     * @param kerberosKdc the new kerberos kdc
     */
    @Autowired
    public void setKerberosKdc(@Value("${cas.spnego.kerb.kdc:172.10.1.10}")
                               final String kerberosKdc) {
        if (StringUtils.isNotBlank(kerberosKdc)) {

            logger.debug("kerberosKdc is set to : {}", kerberosKdc);
            System.setProperty(SYS_PROP_KERBEROS_KDC, kerberosKdc);
        }
    }

    /**
     * Sets the kerberos realm.
     *
     * @param kerberosRealm the new kerberos realm
     */
    @Autowired
    public void setKerberosRealm(@Value("${cas.spnego.kerb.realm:EXAMPLE.COM}")
                                 final String kerberosRealm) {
        if (StringUtils.isNotBlank(kerberosRealm)) {
            logger.debug("kerberosRealm is set to :{}", kerberosRealm);
            System.setProperty(SYS_PROP_KERBEROS_REALM, kerberosRealm);
        }
    }

    @Autowired
    public void setLoginConf(@Value("${cas.spnego.login.conf.file:/path/to/login.conf}")
                             final String loginConf) {
        this.loginConf = loginConf;
    }

    /**
     * Sets the use subject creds only.
     *
     * @param useSubjectCredsOnly the new use subject creds only
     */
    @Autowired
    public void setUseSubjectCredsOnly(@Value("${cas.spnego.use.subject.creds:false}")
                                       final boolean useSubjectCredsOnly) {
        logger.debug("useSubjectCredsOnly is set to {}", useSubjectCredsOnly);
        System.setProperty(SYS_PROP_USE_SUBJECT_CRED_ONLY, Boolean.toString(useSubjectCredsOnly));
    }

    /**
     * Sets the kerberos debug.
     *
     * @param kerberosDebug the new kerberos debug
     */
    @Autowired
    public void setKerberosDebug(@Value("${cas.spnego.kerb.debug:false}")
                                 final String kerberosDebug) {
        if (StringUtils.isNotBlank(kerberosDebug)) {
            logger.debug("kerberosDebug is set to : {}", kerberosDebug);
            System.setProperty(SYS_PROP_KERBEROS_DEBUG, kerberosDebug);
        }
    }

    /**
     * @param jcifsDomain the jcifsDomain to set
     */
    @Autowired
    public void setJcifsDomain(@Value("${cas.spnego.jcifs.domain:}")
                               final String jcifsDomain) {
        if (StringUtils.isNotBlank(jcifsDomain)) {
            logger.debug("jcifsDomain is set to {}", jcifsDomain);
            Config.setProperty(JCIFS_PROP_CLIENT_DOMAIN, jcifsDomain);
        }
    }

    /**
     * @param jcifsDomainController the jcifsDomainController to set
     */
    @Autowired
    public void setJcifsDomainController(@Value("${cas.spnego.jcifs.domaincontroller:}")
                                         final String jcifsDomainController) {
        if (StringUtils.isNotBlank(jcifsDomainController)) {
            logger.debug("jcifsDomainController is set to {}", jcifsDomainController);
            Config.setProperty(JCIFS_PROP_DOMAIN_CONTROLLER, jcifsDomainController);
        }
    }

    /**
     * @param jcifsPassword the jcifsPassword to set
     */
    @Autowired
    public void setJcifsPassword(@Value("${cas.spnego.jcifs.password:}")
                                     final String jcifsPassword) {
        if (StringUtils.isNotBlank(jcifsPassword)) {
            Config.setProperty(JCIFS_PROP_CLIENT_PASSWORD, jcifsPassword);
            logger.debug("jcifsPassword is set to *****");
        }
    }

    /**
     * @param jcifsUsername the jcifsUsername to set
     */
    @Autowired
    public void setJcifsUsername(@Value("${cas.spnego.jcifs.username:}") final String jcifsUsername) {
        if (StringUtils.isNotBlank(jcifsUsername)) {
            logger.debug("jcifsUsername is set to {}", jcifsUsername);
            Config.setProperty(JCIFS_PROP_CLIENT_USERNAME, jcifsUsername);
        }
    }

    /**
     * @param jcifsNetbiosWins the jcifsNetbiosWins to set
     */
    @Autowired
    public void setJcifsNetbiosWins(@Value("${cas.spnego.jcifs.netbios.wins:}")
                                    final String jcifsNetbiosWins) {
        if (StringUtils.isNotBlank(jcifsNetbiosWins)) {
            logger.debug("jcifsNetbiosWins is set to {}", jcifsNetbiosWins);
            Config.setProperty(JCIFS_PROP_NETBIOS_WINS, jcifsNetbiosWins);
        }
    }

    /**
     * Sets jcifs netbios cache policy.
     *
     * @param policy the policy
     */
    @Autowired
    public void setJcifsNetbiosCachePolicy(@Value("${cas.spnego.jcifs.netbios.cache.policy:600}")
                                           final String policy) {
        if (StringUtils.isNotBlank(policy)) {
            logger.debug("jcifsNetbiosCachePolicy is set to {}", policy);
            Config.setProperty(JCIFS_PROP_NETBIOS_CACHE_POLICY, policy);
        }
    }

    /**
     * Sets jcifs socket timeout.
     *
     * @param timeout the timeout
     */
    @Autowired
    public void setJcifsSocketTimeout(@Value("${cas.spnego.jcifs.socket.timeout:300000}")
                                          final String timeout) {
        if (StringUtils.isNotBlank(timeout)) {
            logger.debug("jcifsSocketTimeout is set to {}", timeout);
            Config.setProperty(JCIFS_PROP_CLIENT_SOTIMEOUT, timeout);
        }
    }
}
