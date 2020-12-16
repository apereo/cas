package org.apereo.cas.configuration.model.support.spnego;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
@JsonFilter("SpnegoSystemProperties")
public class SpnegoSystemProperties implements Serializable {
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
     * The Kerberos kdc.
     */
    private String kerberosKdc = "172.10.1.10";

    /**
     * The Kerberos realm.
     */
    private String kerberosRealm = "EXAMPLE.COM";

    /**
     * The Kerberos debug.
     */
    private String kerberosDebug;

    /**
     * The Use subject creds only.
     */
    private boolean useSubjectCredsOnly;
}
