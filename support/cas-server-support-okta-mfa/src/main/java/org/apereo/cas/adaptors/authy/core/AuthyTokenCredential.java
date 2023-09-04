package org.apereo.cas.adaptors.authy.core;

import org.apereo.cas.authentication.credential.OneTimeTokenCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link AuthyTokenCredential}.
 *
 * @author Jérémie POISSON
 * 
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuthyTokenCredential extends OneTimeTokenCredential {
    private static final long serialVersionUID = -7970600701132111037L;

    public AuthyTokenCredential(final String token) {
        super(token);
    }
}
