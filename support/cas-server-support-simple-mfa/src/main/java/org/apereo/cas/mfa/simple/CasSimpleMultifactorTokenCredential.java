package org.apereo.cas.mfa.simple;

import org.apereo.cas.authentication.credential.OneTimeTokenCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link CasSimpleMultifactorTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CasSimpleMultifactorTokenCredential extends OneTimeTokenCredential {
    private static final long serialVersionUID = -4245600701132111037L;

    public CasSimpleMultifactorTokenCredential(final String token) {
        super(token);
    }
}
