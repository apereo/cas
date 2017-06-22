package org.apereo.cas.configuration.model.support.sqrl;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;

/**
 * This is {@link SqrlAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlAuthenticationProperties {
    private String aesKey;

    private Jpa jpa = new Jpa();

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(final String aesKey) {
        this.aesKey = aesKey;
    }
    
    public Jpa getJpa() {
        return jpa;
    }

    public void setJpa(final Jpa jpa) {
        this.jpa = jpa;
    }

    public static class Jpa extends AbstractJpaProperties {}
}
