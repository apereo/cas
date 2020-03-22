package org.apereo.cas.support.spnego.authentication.handler.support;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ResourceLoader;

import java.util.Properties;

/**
 * Configuration helper for JCIFS and the Spring framework.
 *
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @author Scott Battaglia
 * @since 4.2.0
 */
@Slf4j
@Getter
public class JcifsConfig {
    /**
     * Default system configuration that controls the setting of system properties.
     */
    public static final SystemSettings SYSTEM_SETTINGS = new SystemSettings();

    private final JcifsSettings jcifsSettings = new JcifsSettings();

    /**
     * Individual properties collected from CAS settings for each authentication attempt and object.
     * These settings are fed to the Spnego authentication object
     */
    public static class SystemSettings {

        /**
         * Initialize.
         *
         * @param resourceLoader the resource loader
         * @param loginConf      the login conf
         */
        @SneakyThrows
        public static void initialize(final ResourceLoader resourceLoader, final String loginConf) {
            val propValue = System.getProperty(JcifsConfigConstants.SYS_PROP_LOGIN_CONF);
            if (StringUtils.isNotBlank(propValue)) {
                LOGGER.info("Found login config [{}] in system property [{}]", propValue, JcifsConfigConstants.SYS_PROP_LOGIN_CONF);
                if (StringUtils.isNotBlank(loginConf)) {
                    LOGGER.warn("Configured login config for CAS under [{}] will be ignored", loginConf);
                }
            } else {
                val effectiveLoginConf = StringUtils.isBlank(loginConf) ? "/login.conf" : loginConf;
                LOGGER.debug("Attempting to load login config from [{}]", loginConf);

                val res = resourceLoader.getResource(effectiveLoginConf);
                if (res.exists()) {
                    val urlPath = res.getURL().toExternalForm();
                    LOGGER.debug("Located login config [{}] and configured it under [{}]", urlPath, JcifsConfigConstants.SYS_PROP_LOGIN_CONF);
                    System.setProperty(JcifsConfigConstants.SYS_PROP_LOGIN_CONF, urlPath);
                } else {
                    val url = JcifsConfig.class.getResource("/jcifs/http/login.conf");
                    if (url != null) {
                        val fullUrl = url.toExternalForm();
                        LOGGER.debug("Falling back unto default login config [{}] under [{}]", fullUrl, JcifsConfigConstants.SYS_PROP_LOGIN_CONF);
                        System.setProperty(JcifsConfigConstants.SYS_PROP_LOGIN_CONF, fullUrl);
                    }
                }
                LOGGER.debug("configured login configuration path : [{}]", propValue);
            }
        }

        /**
         * Sets the kerberos conf.
         *
         * @param kerberosConf the new kerberos conf
         */
        public static void setKerberosConf(final String kerberosConf) {
            if (StringUtils.isNotBlank(kerberosConf)) {
                LOGGER.debug("kerberosConf is set to :[{}]", kerberosConf);
                System.setProperty(JcifsConfigConstants.SYS_PROP_KERBEROS_CONF, kerberosConf);
            }
        }

        /**
         * Sets the kerberos kdc.
         *
         * @param kerberosKdc the new kerberos kdc
         */
        public static void setKerberosKdc(final String kerberosKdc) {
            if (StringUtils.isNotBlank(kerberosKdc)) {
                LOGGER.debug("kerberosKdc is set to : [{}]", kerberosKdc);
                System.setProperty(JcifsConfigConstants.SYS_PROP_KERBEROS_KDC, kerberosKdc);
            }
        }

        /**
         * Sets the kerberos realm.
         *
         * @param kerberosRealm the new kerberos realm
         */
        public static void setKerberosRealm(final String kerberosRealm) {
            if (StringUtils.isNotBlank(kerberosRealm)) {
                LOGGER.debug("kerberosRealm is set to :[{}]", kerberosRealm);
                System.setProperty(JcifsConfigConstants.SYS_PROP_KERBEROS_REALM, kerberosRealm);
            }
        }

        /**
         * Sets the use subject creds only.
         *
         * @param useSubjectCredsOnly the new use subject creds only
         */
        public static void setUseSubjectCredsOnly(final boolean useSubjectCredsOnly) {
            LOGGER.debug("useSubjectCredsOnly is set to [{}]", useSubjectCredsOnly);
            System.setProperty(JcifsConfigConstants.SYS_PROP_USE_SUBJECT_CRED_ONLY, Boolean.toString(useSubjectCredsOnly));
        }

        /**
         * Sets the kerberos debug.
         *
         * @param kerberosDebug the new kerberos debug
         */
        public static void setKerberosDebug(final String kerberosDebug) {
            if (StringUtils.isNotBlank(kerberosDebug)) {
                LOGGER.debug("kerberosDebug is set to : [{}]", kerberosDebug);
                System.setProperty(JcifsConfigConstants.SYS_PROP_KERBEROS_DEBUG, kerberosDebug);
            }
        }
    }

    /**
     * Individual JCIFS settings tied to spnego authentication objects.
     */
    @Getter
    public static class JcifsSettings {
        private final Properties properties;

        public JcifsSettings() {
            this.properties = new Properties();
            properties.setProperty(JcifsConfigConstants.JCIFS_PROP_CLIENT_SOTIMEOUT, "300000");
            properties.setProperty(JcifsConfigConstants.JCIFS_PROP_NETBIOS_CACHE_POLICY, "600");
        }

        /**
         * Sets the jcifs service password.
         *
         * @param jcifsServicePassword the new jcifs service password
         */
        public void setJcifsServicePassword(final String jcifsServicePassword) {
            if (StringUtils.isNotBlank(jcifsServicePassword)) {
                LOGGER.debug("jcifsServicePassword is set");
                properties.setProperty(JcifsConfigConstants.JCIFS_PROP_SERVICE_PASSWORD, jcifsServicePassword);
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
                properties.setProperty(JcifsConfigConstants.JCIFS_PROP_SERVICE_PRINCIPAL, jcifsServicePrincipal);
            }
        }


        /**
         * @param jcifsDomain the jcifsDomain to set
         */
        public void setJcifsDomain(final String jcifsDomain) {
            if (StringUtils.isNotBlank(jcifsDomain)) {
                LOGGER.debug("jcifsDomain is set to [{}]", jcifsDomain);
                properties.setProperty(JcifsConfigConstants.JCIFS_PROP_CLIENT_DOMAIN, jcifsDomain);
            }
        }

        /**
         * @param jcifsDomainController the jcifsDomainController to set
         */
        public void setJcifsDomainController(final String jcifsDomainController) {
            if (StringUtils.isNotBlank(jcifsDomainController)) {
                LOGGER.debug("jcifsDomainController is set to [{}]", jcifsDomainController);
                properties.setProperty(JcifsConfigConstants.JCIFS_PROP_DOMAIN_CONTROLLER, jcifsDomainController);
            }
        }

        /**
         * @param jcifsPassword the jcifsPassword to set
         */
        public void setJcifsPassword(final String jcifsPassword) {
            if (StringUtils.isNotBlank(jcifsPassword)) {
                properties.setProperty(JcifsConfigConstants.JCIFS_PROP_CLIENT_PASSWORD, jcifsPassword);
            }
        }

        /**
         * @param jcifsUsername the jcifsUsername to set
         */
        public void setJcifsUsername(final String jcifsUsername) {
            if (StringUtils.isNotBlank(jcifsUsername)) {
                LOGGER.debug("jcifsUsername is set to [{}]", jcifsUsername);
                properties.setProperty(JcifsConfigConstants.JCIFS_PROP_CLIENT_USERNAME, jcifsUsername);
            }
        }

        /**
         * @param jcifsNetbiosWins the jcifsNetbiosWins to set
         */
        public void setJcifsNetbiosWins(final String jcifsNetbiosWins) {
            if (StringUtils.isNotBlank(jcifsNetbiosWins)) {
                LOGGER.debug("jcifsNetbiosWins is set to [{}]", jcifsNetbiosWins);
                properties.setProperty(JcifsConfigConstants.JCIFS_PROP_NETBIOS_WINS, jcifsNetbiosWins);
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
                properties.setProperty(JcifsConfigConstants.JCIFS_PROP_NETBIOS_CACHE_POLICY, String.valueOf(policy));
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
                properties.setProperty(JcifsConfigConstants.JCIFS_PROP_CLIENT_SOTIMEOUT, String.valueOf(timeout));
            }
        }
    }
}
