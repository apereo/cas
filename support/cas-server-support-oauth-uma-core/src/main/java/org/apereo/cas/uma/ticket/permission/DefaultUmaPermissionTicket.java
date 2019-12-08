package org.apereo.cas.uma.ticket.permission;

import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSet;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * This is {@link DefaultUmaPermissionTicket}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@Entity
@DiscriminatorValue(UmaPermissionTicket.PREFIX)
@NoArgsConstructor(force = true)
public class DefaultUmaPermissionTicket extends AbstractTicket implements UmaPermissionTicket {
    private static final long serialVersionUID = 2963749819727757623L;
    private static final int MAP_SIZE = 8;

    @Lob
    @Column
    private LinkedHashMap<String, Object> claims = new LinkedHashMap<>(MAP_SIZE);

    @Lob
    @Column
    private LinkedHashSet<String> scopes = new LinkedHashSet<>(MAP_SIZE);

    @Lob
    @Column
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
