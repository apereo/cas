package org.apereo.cas.configuration.model.support.spnego;

import org.apereo.cas.configuration.support.RequiresModule;

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
public class SpnegoSystemProperties implements Serializable {
    private static final long serialVersionUID = -7213507143858237596L;
    /**
     * The Login conf.
     */
    private String loginConf;

    /**
     * The Kerberos conf.
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
