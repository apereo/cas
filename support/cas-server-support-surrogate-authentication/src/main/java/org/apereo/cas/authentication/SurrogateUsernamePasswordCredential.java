package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * This is {@link SurrogateUsernamePasswordCredential},
 * able to substitute a target username on behalf of the given credentials.
 *
 * @author Jonathan Johnson
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString(callSuper = true)
public class SurrogateUsernamePasswordCredential extends RememberMeUsernamePasswordCredential {

    private static final long serialVersionUID = 8760695298971444249L;

    private String surrogateUsername;

    public String getSurrogateUsername() {
        return surrogateUsername;
    }

    public void setSurrogateUsername(final String surrogateUsername) {
        this.surrogateUsername = surrogateUsername;
    }
}
