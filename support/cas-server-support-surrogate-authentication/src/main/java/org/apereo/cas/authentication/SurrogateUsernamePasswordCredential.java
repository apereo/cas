package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link SurrogateUsernamePasswordCredential},
 * able to substitute a target username on behalf of the given credentials.
 *
 * @author Jonathan Johnson
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class SurrogateUsernamePasswordCredential extends RememberMeUsernamePasswordCredential {
    private static final long serialVersionUID = 8760695298971444249L;

    private String surrogateUsername;
}
