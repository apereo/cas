package org.apereo.cas.adaptors.swivel;

import org.apereo.cas.authentication.credential.OneTimeTokenCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link SwivelTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SwivelTokenCredential extends OneTimeTokenCredential {
    private static final long serialVersionUID = 361318678073819595L;

    public SwivelTokenCredential(final String token) {
        super(token);
    }
}
