package org.apereo.cas.configuration.model.support.spnego;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SpnegoSystemProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-spnego")
@Getter
@Setter
@Accessors(chain = true)
public class SpnegoSystemProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -7213507143858237596L;

    /**
     * The Login conf.Absolute path to the jaas login configuration file.
     * This should define the spnego authentication details.
     * Make sure you have at least specified the JCIFS Service Principal defined.
     */
    private String loginConf;

    /**
     * The Kerberos conf.
     * As with all Kerberos installations, a Kerberos Key Distribution Center (KDC) is required.
     * It needs to contain the user name and password you will use to be authenticated to Kerberos.
     * As with most Kerberos installations, a Kerberos configuration file krb5.conf is
     * consulted to determine such things as the default realm and KDC.
     * Typically, the default realm and the KDC for that realm are indicated in
     * the Kerberos krb5.conf configuration file.
     * The path to the configuration file must typically be defined
     * as an absolute path.
     */
    private String kerberosConf;

    /**
     * The Kerberos kdc, set as the {@code java.security.krb5.kdc} system property.
     * When both this and the Kerberos realm are set, the JDK ignores the Kerberos configuration file,
     * and a KDC listening on a port other than 88 cannot be reached since the property carries no port.
     * Leave both blank to take the realm and KDC from the Kerberos configuration file.
     */
    private String kerberosKdc = "172.10.1.10";

    /**
     * The Kerberos realm, set as the {@code java.security.krb5.realm} system property.
     * See the Kerberos kdc setting for how both interact with the Kerberos configuration file.
     */
    private String kerberosRealm = "EXAMPLE.COM";

    /**
     * The Kerberos debug.
     */
    private String kerberosDebug;

    /**
     * Sets the {@code javax.security.auth.useSubjectCredsOnly} system property.
     * When {@code true}, CAS logs the service principal in through the {@code jcifs.spnego.accept} entry of the
     * JAAS login configuration, with the JCIFS service principal and service password, and uses the credentials of
     * that login to accept SPNEGO tokens. When {@code false}, the JCIFS service password and the
     * {@code jcifs.spnego.accept} entry are not used; the JDK finds the service keys on its own,
     * typically from the {@code default_keytab_name} keytab of the Kerberos configuration file.
     */
    private boolean useSubjectCredsOnly;
}
