package org.apereo.cas.support.events.jpa;

import org.apereo.cas.support.events.dao.CasEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;

/**
 * This is {@link JpaCasEvent}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@Entity
@Setter
@Table(name = "CasEvent")
@Accessors(chain = true)
@ToString(callSuper = true)
public class JpaCasEvent extends CasEvent {
    @Serial
    private static final long serialVersionUID = -1176976165442671412L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @Override
    public CasEvent setId(final long id) {
        super.setId(id);
        this.id = id;
        return this;
    }
}
