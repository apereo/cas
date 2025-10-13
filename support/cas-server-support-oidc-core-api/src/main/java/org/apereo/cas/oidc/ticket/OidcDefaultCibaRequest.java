package org.apereo.cas.oidc.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import java.io.Serial;
import java.util.Set;

/**
 * This is {@link OidcDefaultCibaRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
public class OidcDefaultCibaRequest extends AbstractTicket implements OidcCibaRequest {
    @Serial
    private static final long serialVersionUID = 2209847487395823514L;

    @JsonIgnore
    private final String prefix = OidcCibaRequest.PREFIX;

    private final Set<String> scopes;

    private final String clientId;

    private final Authentication authentication;

    private final String encodedId;

    private boolean ready;
    
    public OidcDefaultCibaRequest(final String id,
                                  final Authentication authentication,
                                  final ExpirationPolicy expirationPolicy,
                                  @JsonSetter(nulls = Nulls.AS_EMPTY)
                                  final Set<String> scopes,
                                  final String clientId,
                                  final String encodedId) {
        super(id, expirationPolicy);
        this.scopes = scopes;
        this.clientId = clientId;
        this.authentication = authentication;
        this.encodedId = encodedId;
    }

    @Override
    @CanIgnoreReturnValue
    public OidcCibaRequest markTicketReady() {
        this.ready = true;
        return this;
    }
}
