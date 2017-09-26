package org.apereo.cas.configuration.model.support.sqrl;

import org.apereo.cas.configuration.support.RequiredModule;

import java.io.Serializable;

/**
 * This is {@link SqrlAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredModule(name = "cas-server-support-sqrl")
public class SqrlAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = 7788819241628970358L;
    /**
     * Expiration timeout of the nut generated.
     */
    private long nutExpirationSeconds = 200L;
    /**
     * The server friendly name of the SQRL server.
     */
    private String sfn = "sqrl-cas";

    public String getSfn() {
        return sfn;
    }

    public void setSfn(final String sfn) {
        this.sfn = sfn;
    }

    public long getNutExpirationSeconds() {
        return nutExpirationSeconds;
    }

    public void setNutExpirationSeconds(final long nutExpirationSeconds) {
        this.nutExpirationSeconds = nutExpirationSeconds;
    }
}
