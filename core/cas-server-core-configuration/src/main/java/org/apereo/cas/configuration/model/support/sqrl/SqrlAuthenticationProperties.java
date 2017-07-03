package org.apereo.cas.configuration.model.support.sqrl;

/**
 * This is {@link SqrlAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlAuthenticationProperties {
    private long nutExpirationSeconds = 200L;
    private String sfn = "sqrl-example";

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
