package org.apereo.cas.mgmt.services.web.beans;

import java.io.Serializable;

/**
 * Abstract bean for attribute release.
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class AbstractRegisteredServiceAttributeReleasePolicyBean implements Serializable {
    private static final long serialVersionUID = -7567470297744895709L;

    private boolean releasePassword;
    private boolean releaseTicket;

    public boolean isReleasePassword() {
        return this.releasePassword;
    }

    public void setReleasePassword(final boolean releasePassword) {
        this.releasePassword = releasePassword;
    }

    public boolean isReleaseTicket() {
        return this.releaseTicket;
    }

    public void setReleaseTicket(final boolean releaseTicket) {
        this.releaseTicket = releaseTicket;
    }
}
