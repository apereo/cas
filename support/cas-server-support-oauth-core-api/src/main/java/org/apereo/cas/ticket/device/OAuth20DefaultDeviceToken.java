package org.apereo.cas.ticket.device;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is {@link OAuth20DefaultDeviceToken}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@NoArgsConstructor(force = true)
@Getter
public class OAuth20DefaultDeviceToken extends AbstractTicket implements OAuth20DeviceToken {
    @Serial
    private static final long serialVersionUID = 2339545346159721563L;

    private final Service service;

    @Setter
    private String userCode;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Set<String> scopes = new HashSet<>();
    
    public OAuth20DefaultDeviceToken(final String id, final Service service,
                                     final ExpirationPolicy expirationPolicy,
                                     @JsonSetter(nulls = Nulls.AS_EMPTY)
                                     final Collection<String> scopes) {
        super(id, expirationPolicy);
        this.service = service;
        this.scopes.addAll(scopes);
    }

    @Override
    public String getPrefix() {
        return OAuth20DeviceToken.PREFIX;
    }
}
