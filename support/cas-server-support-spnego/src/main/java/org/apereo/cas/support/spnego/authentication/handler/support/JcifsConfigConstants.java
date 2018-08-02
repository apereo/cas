package org.apereo.cas.support.spnego.authentication.handler.support;

/**
 * This is {@link JcifsConfigConstants}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public interface JcifsConfigConstants {
    String SYS_PROP_USE_SUBJECT_CRED_ONLY = "javax.security.auth.useSubjectCredsOnly";

    String SYS_PROP_LOGIN_CONF = "java.security.auth.login.config";

    String SYS_PROP_KERBEROS_DEBUG = "sun.security.krb5.debug";

    String SYS_PROP_KERBEROS_CONF = "java.security.krb5.conf";

    String SYS_PROP_KERBEROS_REALM = "java.security.krb5.realm";

    String SYS_PROP_KERBEROS_KDC = "java.security.krb5.kdc";

    String JCIFS_PROP_DOMAIN_CONTROLLER = "jcifs.http.domainController";

    String JCIFS_PROP_NETBIOS_WINS = "jcifs.netbios.wins";

    String JCIFS_PROP_CLIENT_DOMAIN = "jcifs.smb.client.domain";

    String JCIFS_PROP_CLIENT_USERNAME = "jcifs.smb.client.username";

    String JCIFS_PROP_CLIENT_PASSWORD = "jcifs.smb.client.password";

    String JCIFS_PROP_CLIENT_SOTIMEOUT = "jcifs.smb.client.soTimeout";

    String JCIFS_PROP_NETBIOS_CACHE_POLICY = "jcifs.netbios.cachePolicy";

    /**
     * -- the service principal you just created. Using the previous example,
     * this would be "HTTP/mybox at DOMAIN.COM".
     */
    String JCIFS_PROP_SERVICE_PRINCIPAL = "jcifs.spnego.servicePrincipal";

    /**
     * The password for the service principal account, required only if you
     * decide not to use keytab.
     */
    String JCIFS_PROP_SERVICE_PASSWORD = "jcifs.spnego.servicePassword";
}
