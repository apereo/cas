package org.apereo.cas.uma.ticket.permission;

import module java.base;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is {@link DefaultUmaPermissionTicket}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@NoArgsConstructor(force = true)
public class DefaultUmaPermissionTicket extends AbstractTicket implements UmaPermissionTicket {
    @Serial
    private static final long serialVersionUID = 2963749819727757623L;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, Object> claims = new LinkedHashMap<>();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Set<String> scopes = new LinkedHashSet<>();

    private ResourceSet resourceSet = new ResourceSet();

    public DefaultUmaPermissionTicket(final String id, final ResourceSet resourceSet,
                                      final ExpirationPolicy expirationPolicy,
                                      @JsonSetter(nulls = Nulls.AS_EMPTY)
                                      final Collection<String> scopes,
                                      final Map<String, Object> claims) {
        super(id, expirationPolicy);
        this.resourceSet = resourceSet;
        this.scopes = new LinkedHashSet<>(scopes);
        this.claims = new LinkedHashMap<>(claims);
    }

    @Override
    public String getPrefix() {
        return UmaPermissionTicket.PREFIX;
    }
}
