package org.apereo.cas.mfa.twilio;

import org.apereo.cas.authentication.credential.OneTimeTokenCredential;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.io.Serial;

/**
 * This is {@link CasTwilioMultifactorTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CasTwilioMultifactorTokenCredential extends OneTimeTokenCredential {
    @Serial
    private static final long serialVersionUID = -4245611701132111037L;

    public CasTwilioMultifactorTokenCredential(final String token) {
        super(token);
    }
}
