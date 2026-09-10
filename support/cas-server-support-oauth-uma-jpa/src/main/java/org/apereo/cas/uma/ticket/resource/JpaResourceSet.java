package org.apereo.cas.uma.ticket.resource;

import module java.base;
import lombok.Getter;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * This is {@link JpaResourceSet}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Table(name = "UMA_ResourceSet")
@Entity
@Getter
public class JpaResourceSet extends ResourceSet {
    @Serial
    private static final long serialVersionUID = -592895072654246305L;

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private long id;

    @Override
    public void setId(final long id) {
        super.setId(id);
        this.id = id;
    }
}
