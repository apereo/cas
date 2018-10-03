package org.apereo.cas.adaptors.u2f;

import org.apereo.cas.authentication.credential.OneTimeTokenCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link U2FTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class U2FTokenCredential extends OneTimeTokenCredential {
    private static final long serialVersionUID = -970682410132111037L;

    public U2FTokenCredential(final String token) {
        super(token);
    }
}
