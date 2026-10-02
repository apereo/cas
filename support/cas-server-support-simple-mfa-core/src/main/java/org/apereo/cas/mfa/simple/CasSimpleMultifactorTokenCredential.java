package org.apereo.cas.mfa.simple;

import module java.base;
import org.apereo.cas.authentication.credential.OneTimeTokenCredential;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link CasSimpleMultifactorTokenCredential}.
 * The token is the code the user types. The ticket id, when present, is the id the ticket registry stored
 * the token under, as the webflow recorded it when the token was sent; it is never bound from the request.
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
    @Serial
    private static final long serialVersionUID = -4245600701132111037L;

    private @Nullable String ticketId;

    public CasSimpleMultifactorTokenCredential(final String token) {
        super(token);
    }
}
