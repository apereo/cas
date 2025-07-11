package org.apereo.cas.uma.ticket.resource;

import lombok.Getter;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;

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
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @Override
    public void setId(final long id) {
        super.setId(id);
        this.id = id;
    }
}
