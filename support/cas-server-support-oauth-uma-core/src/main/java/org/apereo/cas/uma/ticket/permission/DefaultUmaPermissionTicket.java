package org.apereo.cas.uma.ticket.permission;

import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSet;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
    private static final long serialVersionUID = 2963749819727757623L;

    private Map<String, Object> claims = new LinkedHashMap<>();

    private Set<String> scopes = new LinkedHashSet<>();

    private ResourceSet resourceSet = new ResourceSet();

    public DefaultUmaPermissionTicket(final String id, final ResourceSet resourceSet,
                                      final ExpirationPolicy expirationPolicy,
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
