package org.apereo.cas.support.spnego.authentication.handler.support;

/**
 * This is {@link JcifsConfigConstants}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public interface JcifsConfigConstants {
    /**
     * When false, allows us to relax the usual restriction of requiring a GSS mechanism to obtain
     * necessary credentials from an existing Subject, set up by JAAS. When this restriction is relaxed,
     * it allows the mechanism to obtain credentials from some vendor-specific location. For example,
     * some vendors might choose to use the operating system's cache if one exists, while others might
     * choose to read from a protected file on disk.
     */
    String SYS_PROP_USE_SUBJECT_CRED_ONLY = "javax.security.auth.useSubjectCredsOnly";

    /**
     * Path to the login.config JAAS file.
     */
    String SYS_PROP_LOGIN_CONF = "java.security.auth.login.config";

    /**
     * Turn on debug for kerberos.
     */
    String SYS_PROP_KERBEROS_DEBUG = "sun.security.krb5.debug";

    /**
     * Path to the kerberos config file.
     */
    String SYS_PROP_KERBEROS_CONF = "java.security.krb5.conf";

    /**
     * Definition of the kerberos realm.
     */
    String SYS_PROP_KERBEROS_REALM = "java.security.krb5.realm";

    /**
     * Definition of the kerberos KDC.
     */
    String SYS_PROP_KERBEROS_KDC = "java.security.krb5.kdc";

    /**
     * The DNS hostname or IP address of a server that should be used to authenticate HTTP
     * clients with the NtlmSsp class (use by NtlmHttpFilter and NetworkExplorer). If this is not
     * specified the jcifs.smb.client.domain 0x1C NetBIOS group name will be queried. It is not necessary
     * for this to specify a real domain controller. The IP address of a workstation will do for development purposes.
     */
    String JCIFS_PROP_DOMAIN_CONTROLLER = "jcifs.http.domainController";

    /**
     * The IP address of the WINS server. This is only required when accessing hosts on
     * different subnets although it is recommended if a WINS server is provided.
     */
    String JCIFS_PROP_NETBIOS_WINS = "jcifs.netbios.wins";

    /**
     * The default authentication domain used if not specified in an SMB URL.
     */
    String JCIFS_PROP_CLIENT_DOMAIN = "jcifs.smb.client.domain";

    /**
     * The default username used if not specified in an SMB URL.
     */
    String JCIFS_PROP_CLIENT_USERNAME = "jcifs.smb.client.username";

    /**
     * The default password used if not specified in an SMB URL.
     */
    String JCIFS_PROP_CLIENT_PASSWORD = "jcifs.smb.client.password";

    /**
     * To prevent the client from holding server resources unnecessarily, sockets are
     * closed after this time period if there is no activity. This time is specified in milliseconds. The default is 35000.
     */
    String JCIFS_PROP_CLIENT_SOTIMEOUT = "jcifs.smb.client.soTimeout";

    /**
     * When a NetBIOS name is resolved with the NbtAddress class it is cached to reduce
     * redundant name queries. This property controls how long, in seconds, these names are
     * cached. The default is 30 seconds, 0 is no caching, and -1 is forever.
     */
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
