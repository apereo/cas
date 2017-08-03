package org.apereo.cas.support.spnego.authentication.handler.support;

import jcifs.Config;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;
import java.net.URL;

/**
 * Configuration helper for JCIFS and the Spring framework.
 *
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @author Scott Battaglia
 * @since 4.2.0
 */
public class JcifsConfig {

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

    private static final Logger LOGGER = LoggerFactory.getLogger(JcifsConfig.class);

    private String loginConf;
    
    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Instantiates a new jCIFS config.
     */
    public JcifsConfig() {
        Config.setProperty(JCIFS_PROP_CLIENT_SOTIMEOUT, "300000");
        Config.setProperty(JCIFS_PROP_NETBIOS_CACHE_POLICY, "600");
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        configureJaasLoginConfig();
    }

    /**
     * Configure jaas login config location and set it as a system property.
     */
    protected void configureJaasLoginConfig() {
        try {
            final String propValue = System.getProperty(SYS_PROP_LOGIN_CONF);
            if (StringUtils.isNotBlank(propValue)) {
                LOGGER.info("Found login config [{}] in system property [{}]", propValue, SYS_PROP_LOGIN_CONF);
                if (StringUtils.isNotBlank(this.loginConf)) {
                    LOGGER.warn("Configured login config for CAS under [{}] will be ignored", this.loginConf);
                }
            } else {
                final String loginConf = StringUtils.isBlank(this.loginConf) ? DEFAULT_LOGIN_CONFIG : this.loginConf;
                LOGGER.debug("Attempting to load login config from [{}]", loginConf);

                final Resource res = this.resourceLoader.getResource(loginConf);
                if (res != null && res.exists()) {
                    final String urlPath = res.getURL().toExternalForm();
                    LOGGER.debug("Located login config [{}] and configured it under [{}]", urlPath, SYS_PROP_LOGIN_CONF);
                    System.setProperty(SYS_PROP_LOGIN_CONF, urlPath);
                } else {
                    final URL url = getClass().getResource("/jcifs/http/login.conf");
                    if (url != null) {
                        LOGGER.debug("Falling back unto default login config [{}] under [{}]", url.toExternalForm(), SYS_PROP_LOGIN_CONF);
                        System.setProperty(SYS_PROP_LOGIN_CONF, url.toExternalForm());
                    }
                }
                LOGGER.debug("configured login configuration path : [{}]", propValue);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Sets the jcifs service password.
     *
     * @param jcifsServicePassword the new jcifs service password
     */
    public void setJcifsServicePassword(final String jcifsServicePassword) {
        if (StringUtils.isNotBlank(jcifsServicePassword)) {
            LOGGER.debug("jcifsServicePassword is set to *****");
            Config.setProperty(JCIFS_PROP_SERVICE_PASSWORD, jcifsServicePassword);
        }
    }

    /**
     * Sets the jcifs service principal.
     *
     * @param jcifsServicePrincipal the new jcifs service principal
     */
    public void setJcifsServicePrincipal(final String jcifsServicePrincipal) {
        if (StringUtils.isNotBlank(jcifsServicePrincipal)) {
            LOGGER.debug("jcifsServicePrincipal is set to [{}]", jcifsServicePrincipal);
            Config.setProperty(JCIFS_PROP_SERVICE_PRINCIPAL, jcifsServicePrincipal);
        }
    }

    /**
     * Sets the kerberos conf.
     *
     * @param kerberosConf the new kerberos conf
     */
    public void setKerberosConf(final String kerberosConf) {
        if (StringUtils.isNotBlank(kerberosConf)) {

            LOGGER.debug("kerberosConf is set to :[{}]", kerberosConf);
            System.setProperty(SYS_PROP_KERBEROS_CONF, kerberosConf);
        }
    }

    /**
     * Sets the kerberos kdc.
     *
     * @param kerberosKdc the new kerberos kdc
     */
    public void setKerberosKdc(final String kerberosKdc) {
        if (StringUtils.isNotBlank(kerberosKdc)) {
            LOGGER.debug("kerberosKdc is set to : [{}]", kerberosKdc);
            System.setProperty(SYS_PROP_KERBEROS_KDC, kerberosKdc);
        }
    }

    /**
     * Sets the kerberos realm.
     *
     * @param kerberosRealm the new kerberos realm
     */
    public void setKerberosRealm(final String kerberosRealm) {
        if (StringUtils.isNotBlank(kerberosRealm)) {
            LOGGER.debug("kerberosRealm is set to :[{}]", kerberosRealm);
            System.setProperty(SYS_PROP_KERBEROS_REALM, kerberosRealm);
        }
    }
    
    public void setLoginConf(final String loginConf) {
        this.loginConf = loginConf;
    }

    /**
     * Sets the use subject creds only.
     *
     * @param useSubjectCredsOnly the new use subject creds only
     */
    public void setUseSubjectCredsOnly(final boolean useSubjectCredsOnly) {
        LOGGER.debug("useSubjectCredsOnly is set to [{}]", useSubjectCredsOnly);
        System.setProperty(SYS_PROP_USE_SUBJECT_CRED_ONLY, Boolean.toString(useSubjectCredsOnly));
    }

    /**
     * Sets the kerberos debug.
     *
     * @param kerberosDebug the new kerberos debug
     */
    public void setKerberosDebug(final String kerberosDebug) {
        if (StringUtils.isNotBlank(kerberosDebug)) {
            LOGGER.debug("kerberosDebug is set to : [{}]", kerberosDebug);
            System.setProperty(SYS_PROP_KERBEROS_DEBUG, kerberosDebug);
        }
    }

    /**
     * @param jcifsDomain the jcifsDomain to set
     */
    public void setJcifsDomain(final String jcifsDomain) {
        if (StringUtils.isNotBlank(jcifsDomain)) {
            LOGGER.debug("jcifsDomain is set to [{}]", jcifsDomain);
            Config.setProperty(JCIFS_PROP_CLIENT_DOMAIN, jcifsDomain);
        }
    }

    /**
     * @param jcifsDomainController the jcifsDomainController to set
     */
    public void setJcifsDomainController(final String jcifsDomainController) {
        if (StringUtils.isNotBlank(jcifsDomainController)) {
            LOGGER.debug("jcifsDomainController is set to [{}]", jcifsDomainController);
            Config.setProperty(JCIFS_PROP_DOMAIN_CONTROLLER, jcifsDomainController);
        }
    }

    /**
     * @param jcifsPassword the jcifsPassword to set
     */
    public void setJcifsPassword(final String jcifsPassword) {
        if (StringUtils.isNotBlank(jcifsPassword)) {
            Config.setProperty(JCIFS_PROP_CLIENT_PASSWORD, jcifsPassword);
            LOGGER.debug("jcifsPassword is set to *****");
        }
    }

    /**
     * @param jcifsUsername the jcifsUsername to set
     */
    public void setJcifsUsername(final String jcifsUsername) {
        if (StringUtils.isNotBlank(jcifsUsername)) {
            LOGGER.debug("jcifsUsername is set to [{}]", jcifsUsername);
            Config.setProperty(JCIFS_PROP_CLIENT_USERNAME, jcifsUsername);
        }
    }

    /**
     * @param jcifsNetbiosWins the jcifsNetbiosWins to set
     */
    public void setJcifsNetbiosWins(final String jcifsNetbiosWins) {
        if (StringUtils.isNotBlank(jcifsNetbiosWins)) {
            LOGGER.debug("jcifsNetbiosWins is set to [{}]", jcifsNetbiosWins);
            Config.setProperty(JCIFS_PROP_NETBIOS_WINS, jcifsNetbiosWins);
        }
    }

    /**
     * Sets jcifs netbios cache policy.
     *
     * @param policy the policy
     */
    public void setJcifsNetbiosCachePolicy(final long policy) {
        if (policy > 0) {
            LOGGER.debug("jcifsNetbiosCachePolicy is set to [{}]", policy);
            Config.setProperty(JCIFS_PROP_NETBIOS_CACHE_POLICY, String.valueOf(policy));
        }
    }

    /**
     * Sets jcifs socket timeout.
     *
     * @param timeout the timeout
     */
    public void setJcifsSocketTimeout(final long timeout) {
        if (timeout > 0) {
            LOGGER.debug("jcifsSocketTimeout is set to [{}]", timeout);
            Config.setProperty(JCIFS_PROP_CLIENT_SOTIMEOUT, String.valueOf(timeout));
        }
    }
}
