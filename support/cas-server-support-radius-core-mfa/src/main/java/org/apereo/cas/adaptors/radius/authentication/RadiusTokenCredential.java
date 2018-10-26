package org.apereo.cas.adaptors.radius.authentication;

import org.apereo.cas.authentication.credential.OneTimeTokenCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link RadiusTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RadiusTokenCredential extends OneTimeTokenCredential {
    private static final long serialVersionUID = -7570675701132111037L;

    public RadiusTokenCredential(final String token) {
        super(token);
    }
}
