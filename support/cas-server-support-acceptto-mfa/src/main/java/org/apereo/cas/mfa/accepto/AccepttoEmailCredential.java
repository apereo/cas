package org.apereo.cas.mfa.accepto;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * This is {@link AccepttoEmailCredential}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AccepttoEmailCredential extends BasicIdentifiableCredential {
    @Serial
    private static final long serialVersionUID = -4245622701132144037L;

    public AccepttoEmailCredential(final String email) {
        super(email);
    }
}
